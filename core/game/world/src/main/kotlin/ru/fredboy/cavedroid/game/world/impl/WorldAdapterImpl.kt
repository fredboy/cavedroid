package ru.fredboy.cavedroid.game.world.impl

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.floor
import ru.fredboy.cavedroid.domain.assets.repository.BlockActionSoundAssetsRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.domain.world.lighting.LightHandle
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerWorldAdapter
import ru.fredboy.cavedroid.entity.container.model.ContainerCoordinates
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.projectile.abstraction.ProjectileWorldAdapter
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.lighting.LightingSystem
import javax.inject.Inject
import kotlin.reflect.KClass

@GameScope
internal class WorldAdapterImpl @Inject constructor(
    private val gameWorld: GameWorld,
    private val itemsRepository: ItemsRepository,
    private val mobParamsRepository: MobParamsRepository,
    private val lightingSystem: LightingSystem,
    private val blockActionSounds: BlockActionSoundAssetsRepository,
    private val soundPlayer: SoundPlayer,
) : DropWorldAdapter,
    ContainerWorldAdapter,
    MobWorldAdapter,
    ProjectileWorldAdapter {

    override val height: Int
        get() = gameWorld.height

    override val seaLevel: Int
        get() = gameWorld.generatorConfig.seaLevel

    @Deprecated("Use start/end")
    override val width: Int
        get() = gameWorld.width

    override val isInfinite: Boolean
        get() = gameWorld.isInfinite

    override fun setForegroundBlock(x: Int, y: Int, block: Block) {
        gameWorld.setForeMap(x, y, block)
    }

    override fun setBackgroundBlock(x: Int, y: Int, block: Block) {
        gameWorld.setBackMap(x, y, block)
    }

    override fun getForegroundBlock(x: Int, y: Int): Block = gameWorld.getForeMap(x, y)

    override fun getBackgroundBlock(x: Int, y: Int): Block = gameWorld.getBackMap(x, y)

    override fun destroyForegroundBlock(x: Int, y: Int, shouldDrop: Boolean, destroyedByPlayer: Boolean) {
        gameWorld.destroyForeMap(x, y, shouldDrop, destroyedByPlayer)
    }

    override fun destroyBackgroundBlock(x: Int, y: Int, shouldDrop: Boolean, destroyedByPlayer: Boolean) {
        gameWorld.destroyBackMap(x, y, shouldDrop, destroyedByPlayer)
    }

    override fun findSpawnPoint(): Vector2 {
        val charParams = mobParamsRepository.getMobParamsByKey("char")

        if (gameWorld.isInfinite) {
            // Infinite worlds spawn at the origin column; the streaming store keeps it resident.
            val spawnX = 0
            var surfaceY = gameWorld.height - 1
            for (sy in 0 until gameWorld.height) {
                if (getForegroundBlock(spawnX, sy).params.hasCollision) {
                    surfaceY = sy
                    break
                }
            }
            return Vector2(spawnX + .5f - (charParams?.width ?: 1f) / 2, surfaceY - (charParams?.height ?: 1f))
        }

        var x = width / 2
        var y = 0

        while (y++ in 0..gameWorld.generatorConfig.seaLevel) {
            if (y == gameWorld.generatorConfig.seaLevel) {
                while (x++ in 0..<width) {
                    if (getForegroundBlock(x, y).params.hasCollision) {
                        break
                    } else if (x == width - 1) {
                        setBackgroundBlock(x, y, itemsRepository.getBlockByKey("grass"))
                        break
                    }
                }
                break
            }
            if (getForegroundBlock(width / 2, y).params.hasCollision) {
                break
            }
        }

        return Vector2(x + .5f - (charParams?.width ?: 1f) / 2, y - (charParams?.height ?: 1f))
    }

    override fun checkContainerAtCoordinates(
        coordinates: ContainerCoordinates,
        requiredType: KClass<out Block.Container>,
    ): Boolean {
        val block = when (coordinates.z) {
            Layer.FOREGROUND.z -> gameWorld.getForeMap(coordinates.x, coordinates.y)
            Layer.BACKGROUND.z -> gameWorld.getBackMap(coordinates.x, coordinates.y)
            else -> itemsRepository.fallbackBlock
        }

        return block.isContainer() && block::class == requiredType
    }

    override fun isContainerChunkLoaded(coordinates: ContainerCoordinates): Boolean {
        return gameWorld.isChunkLoadedAt(coordinates.x)
    }

    override fun addOnBlockDestroyedListener(listener: OnBlockDestroyedListener) {
        gameWorld.addBlockDestroyedListener(listener)
    }

    override fun addOnBlockPlacedListener(listener: OnBlockPlacedListener) {
        gameWorld.addBlockPlacedListener(listener)
    }

    override fun removeOnBlockDestroyedListener(listener: OnBlockDestroyedListener) {
        gameWorld.removeBlockDestroyedListener(listener)
    }

    override fun removeOnBlockPlacedListener(listener: OnBlockPlacedListener) {
        gameWorld.removeBlockPlacedListener(listener)
    }

    override fun getBox2dWorld(): World {
        return gameWorld.world
    }

    override fun getClimbable(hitbox: Rectangle): Block.Climbable? {
        val (startX, endX) = hitbox.x.floor.toInt() to (hitbox.x + hitbox.width).floor.toInt()
        val (startY, endY) = hitbox.y.floor.toInt() to (hitbox.y + hitbox.height).floor.toInt()

        var medium: Block.Climbable? = null
        for (x in startX..endX) {
            for (y in startY..endY) {
                val block = (getForegroundBlock(x, y) as? Block.Climbable)
                    ?.takeIf { it.getRectangle(x, y).overlaps(hitbox) }
                    ?: continue

                if (medium == null || medium.climbSpeedFactor < block.climbSpeedFactor) {
                    medium = block
                }
            }
        }
        return medium
    }

    override fun getMediumLiquid(hitbox: Rectangle): Block.Fluid? {
        return getClimbable(hitbox) as? Block.Fluid
    }

    override fun createPlayerSightLight(body: Body, x: Float, y: Float): LightHandle {
        return lightingSystem.createPlayerSightLight(body, x, y)
    }

    override fun createFurnaceLight(x: Float, y: Float): LightHandle {
        return lightingSystem.createFurnaceLight(x, y)
    }

    override fun isChunkLoadedAt(x: Int): Boolean = gameWorld.isChunkLoadedAt(x)

    override fun setWoodenDoorOpen(x: Int, bottomY: Int, open: Boolean): Boolean {
        if (!isChunkLoadedAt(x)) return false
        val key = getForegroundBlock(x, bottomY).params.key
        val side = when {
            key.startsWith("door_wood_bottom_left_") -> "left"
            key.startsWith("door_wood_bottom_right_") -> "right"
            else -> return false
        }
        val oldState = if (open) "closed" else "open"
        val newState = if (open) "open" else "closed"
        if (key != "door_wood_bottom_${side}_$oldState" ||
            getForegroundBlock(x, bottomY - 1).params.key != "door_wood_top_${side}_$oldState"
        ) {
            return false
        }
        if (!open) {
            var occupied = false
            gameWorld.world.QueryAABB({ fixture ->
                if (!fixture.isSensor && fixture.body.type != BodyDef.BodyType.StaticBody) {
                    occupied = true
                }
                !occupied
            }, x.toFloat(), bottomY - 1f, x + 1f, bottomY + 1f)
            if (occupied) return false
        }
        setForegroundBlock(x, bottomY - 1, itemsRepository.getBlockByKey("door_wood_top_${side}_$newState"))
        setForegroundBlock(x, bottomY, itemsRepository.getBlockByKey("door_wood_bottom_${side}_$newState"))
        return true
    }

    override fun isDayTime(): Boolean {
        return gameWorld.isDayTime()
    }

    override fun playBlockActionSound(block: Block, x: Int, y: Int, player: PlayerAdapter) {
        val key = block.params.actionSoundKey ?: return
        val sound = blockActionSounds.getBlockActionSound(key) ?: return
        soundPlayer.playSoundAtPosition(sound, x + 0.5f, y + 0.5f, player.x, player.y)
    }

    override fun canPlaceToForeground(x: Int, y: Int, value: Block): Boolean {
        return gameWorld.canPlaceToForeground(x, y, value)
    }
}
