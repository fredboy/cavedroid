package ru.fredboy.cavedroid.game.controller.explosion

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.repository.BlockActionSoundAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.domain.save.model.TntEntry
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.fire.FireController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.random.Random

@GameScope
class ExplosionController @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val fireController: FireController,
    private val dropQueue: DropQueue,
    private val getItemByKey: GetItemByKeyUseCase,
    private val sounds: BlockActionSoundAssetsRepository,
    private val soundPlayer: SoundPlayer,
    saveDataRepository: SaveDataRepository,
    applicationContextRepository: ApplicationContextRepository,
    gameContextRepository: GameContextRepository,
) : Disposable {
    data class ExplosionEffect(val x: Float, val y: Float, var age: Float = 0f)

    private val activeTnt = mutableListOf<PrimedTnt>()
    private val activeEffects = mutableListOf<ExplosionEffect>()
    val primedTnt: List<PrimedTnt> get() = activeTnt
    val effects: List<ExplosionEffect> get() = activeEffects

    init {
        if (gameContextRepository.isLoadGame()) {
            activeTnt += saveDataRepository.loadTntEntries(
                applicationContextRepository.getGameDirectory(),
                gameContextRepository.getSaveGameDirectory(),
            ).map { PrimedTnt(it.x, it.y, it.velocityX, it.velocityY, it.fuse) }
        }
    }

    fun snapshot(): List<TntEntry> = activeTnt.map { TntEntry(it.x, it.y, it.velocityX, it.velocityY, it.fuse) }

    fun prime(x: Int, y: Int, layer: Layer = Layer.FOREGROUND, fuse: Float = ExplosionRules.FUSE_SECONDS): Boolean {
        if (y !in 0 until gameWorld.height || !gameWorld.isChunkLoadedAt(x)) return false
        if (blockAt(x, y, layer).params.key != "tnt") return false
        destroy(x, y, layer)
        val worldX = if (gameWorld.isInfinite) x else Math.floorMod(x, gameWorld.width)
        activeTnt += PrimedTnt(worldX + 0.5f, y + 0.5f, Random.nextFloat() * 0.8f - 0.4f, -4f, fuse)
        return true
    }

    fun update(delta: Float) {
        activeEffects.forEach { it.age += delta }
        activeEffects.removeAll { it.age >= ExplosionRules.EFFECT_SECONDS }
        fireController.snapshot().filter { blockAt(it.x, it.y, it.layer).params.key == "tnt" }
            .forEach { prime(it.x, it.y, it.layer) }

        // Chain-primed TNT starts on the next update, never during the originating blast.
        activeTnt.toList().forEach { tnt ->
            // Keep the entire possible blast resident; unloaded TNT is saved globally and paused.
            val column = floor(tnt.x).toInt()
            if ((column - 8..column + 8).any { !gameWorld.isChunkLoadedAt(it) }) return@forEach
            var remaining = delta
            while (remaining > 0f && tnt.fuse > 0f) {
                val step = minOf(remaining, tnt.fuse, 0.05f)
                TntPhysics.move(tnt, step, gameWorld::getForeMap)
                if (!gameWorld.isInfinite) {
                    tnt.x -= floor(tnt.x / gameWorld.width) * gameWorld.width
                }
                tnt.fuse -= step
                remaining -= step
            }
            if (tnt.y > gameWorld.height + 1f) {
                activeTnt.remove(tnt)
            } else if (tnt.fuse <= 0f) {
                activeTnt.remove(tnt)
                explode(tnt.x, tnt.y)
            }
        }
    }

    fun explode(x: Float, y: Float) {
        damageEntities(x, y)
        val origin = gameWorld.getForeMap(floor(x).toInt(), floor(y).toInt())
        // Submerged TNT still hurts entities, but the liquid absorbs its terrain damage.
        if (!origin.isFluid()) {
            val affected = Layer.entries.associateWith { layer ->
                ExplosionRules.affectedBlocks(x, y) { bx, by -> blockAt(bx, by, layer) }
                    .map { (bx, by) ->
                        (if (gameWorld.isInfinite) bx else Math.floorMod(bx, gameWorld.width)) to by
                    }.toSet()
            }
            affected.forEach { (layer, blocks) ->
                blocks.forEach { (bx, by) ->
                    if (by !in 0 until gameWorld.height || !gameWorld.isChunkLoadedAt(bx)) return@forEach
                    val block = blockAt(bx, by, layer)
                    if (block.params.key == "tnt") {
                        prime(bx, by, layer, Random.nextInt(10, 31) / 20f)
                    } else if (!block.isNone() && !block.isFluid() && block.params.hitPoints >= 0) {
                        // Explosions meet mining tool requirements without depending on the held item.
                        val drops = block.getDropItems(getItemByKey, toolRequirementMet = block.params.toolType != Item.Shears::class.java)
                        destroy(bx, by, layer)
                        dropQueue.offerItems(bx + 0.5f, by + 0.5f, drops)
                    }
                }
            }
        }
        activeEffects += ExplosionEffect(x, y)
        val player = mobController.player
        sounds.getBlockActionSound("explosion")?.let { sound ->
            val dx = ExplosionRules.horizontalDistance(player.position.x, x, gameWorld.width, gameWorld.isInfinite)
            soundPlayer.playSoundAtPosition(sound, player.position.x + dx, y, player.position.x, player.position.y)
        }
    }

    private fun damageEntities(x: Float, y: Float) {
        (mobController.mobs + mobController.player).forEach { mob ->
            if (mob.isDead || mob is Player && mob.gameMode.isCreative()) return@forEach
            val dx = ExplosionRules.horizontalDistance(x, mob.position.x, gameWorld.width, gameWorld.isInfinite)
            val dy = mob.position.y - y
            val distance = sqrt(dx * dx + dy * dy)
            if (distance > ExplosionRules.TNT_POWER * 2f) return@forEach
            val bounds = Rectangle(x + dx - mob.width / 2f, mob.position.y - mob.height / 2f, mob.width, mob.height)
            val exposure = ExplosionRules.exposure(x, y, bounds, gameWorld::getForeMap)
            val impact = ExplosionRules.impact(distance, exposure)
            mob.damage(ExplosionRules.damage(impact))
            if (distance > 0f) {
                mob.velocity.x += dx / distance * impact * 20f
                mob.velocity.y += dy / distance * impact * 20f
            } else {
                mob.velocity.y -= impact * 20f
            }
        }
    }

    private fun blockAt(x: Int, y: Int, layer: Layer): Block = when (layer) {
        Layer.FOREGROUND -> gameWorld.getForeMap(x, y)
        Layer.BACKGROUND -> gameWorld.getBackMap(x, y)
    }

    private fun destroy(x: Int, y: Int, layer: Layer) {
        when (layer) {
            Layer.FOREGROUND -> gameWorld.destroyForeMap(x, y, shouldDrop = false)
            Layer.BACKGROUND -> gameWorld.destroyBackMap(x, y, shouldDrop = false)
        }
    }

    override fun dispose() {
        activeTnt.clear()
        activeEffects.clear()
    }
}
