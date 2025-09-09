package ru.fredboy.cavedroid.game.controller.mob

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.TooltipManager
import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.domain.assets.repository.StepsSoundAssetsRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.impl.PlayerAdapterImpl_Factory
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min

@GameScope
class MobController @Inject constructor(
    getFallbackItemUseCase: GetFallbackItemUseCase,
    mobParamsRepository: MobParamsRepository,
    private val mobWorldAdapter: MobWorldAdapter,
    private val mobPhysicsFactory: MobPhysicsFactory,
    private val dropQueue: DropQueue,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
    private val tooltipManager: TooltipManager,
    private val mobSoundManager: MobSoundManager,
    private val soundPlayer: SoundPlayer,
    private val stepsSoundAssetsRepository: StepsSoundAssetsRepository,
) : Disposable {

    // TODO: Do proper DI
    private val playerAdapter = PlayerAdapterImpl_Factory.newInstance(this)

    private val _mobs = LinkedList<Mob>()

    val mobs: List<Mob> get() = _mobs

    var player = Player(
        getFallbackItem = getFallbackItemUseCase,
        tooltipManager = tooltipManager,
        params = requireNotNull(mobParamsRepository.getMobParamsByKey("char")),
        soundPlayer = soundPlayer,
        stepsSoundAssetsRepository = stepsSoundAssetsRepository,
    )
        set(value) {
            field.dispose()
            field = value
            field.initSight(mobWorldAdapter)
        }

    init {
        respawnPlayer()
        mobSoundManager.attachToMobController(this)
    }

    fun addMob(mob: Mob) {
        // TODO: Probably shouldn't add if already in the list
        _mobs.add(mob)
    }

    fun removeMob(mob: Mob) {
        _mobs.remove(mob)
    }

    fun update(delta: Float) {
        mobs.forEach { mob ->
            mob.update(mobWorldAdapter, playerAdapter, delta)
            mobSoundManager.makeStepSound(mob)
        }
        _mobs.removeAll { mob ->
            mob.isDead.ifTrue {
                dropQueue.offerItems(mob.position.x, mob.position.y, mob.getDropItems(getItemByKeyUseCase))
                mob.dispose()
                true
            } ?: false
        }

        updatePlayer(delta)
    }

    private fun updatePlayer(delta: Float) {
        player.update(mobWorldAdapter, playerAdapter, delta)
        mobSoundManager.makeStepSound(player)

        limitPlayerCursor()

        if (player.isDead) {
            dropQueue.offerInventory(player.position.x, player.position.y, player.inventory)
            player.inventory.clear()
            player.dispose()
            respawnPlayer()
        }
    }

    fun limitPlayerCursor() {
        with(player) {
            val cursor = Vector2(cursorX, cursorY)
            if (gameMode.isSurvival()) {
                val plToCursor = cursor
                    .sub(position)
                    .limit2(SURVIVAL_CURSOR_RANGE_2)

                cursorX = position.x + plToCursor.x
                cursorY = position.y + plToCursor.y
            }

            cursorY = MathUtils.clamp(cursorY, 0f, mobWorldAdapter.height.toFloat())

            cursor.set(cursorX, cursorY)

            if (position.dst2(cursor) < 0.001f) {
                return
            }

            mobWorldAdapter.getBox2dWorld().rayCast(
                { fixture, point, _, fraction ->
                    val block = fixture.userData as? Block ?: return@rayCast -1f

                    val pointToCursor = cursor.cpy().sub(point)

                    val pointToCursorDX = abs(pointToCursor.x)
                    val pointToCursorDY = abs(pointToCursor.y)

                    if (pointToCursorDX < block.width / 2f && pointToCursorDY < block.height / 2f) {
                        return@rayCast -1f
                    }

                    point.cpy().apply {
                        x += min(pointToCursorDX, block.width / 2f) * if (pointToCursor.x < 0) -1f else 1f
                        y += min(pointToCursorDY, block.height / 2f) * if (pointToCursor.y < 0) -1f else 1f

                        cursorX = x
                        cursorY = y

                        if (player.holdCursor) {
                            player.cursorToPlayer.set(
                                player.cursorX - player.position.x,
                                player.cursorY - player.position.y,
                            )
                        }
                    }

                    return@rayCast fraction
                },
                position,
                cursor,
            )
        }
    }

    fun respawnPlayer() {
        player.respawn(
            spawnPoint = player.spawnPoint ?: mobWorldAdapter.findSpawnPoint(),
            mobPhysicsFactory = mobPhysicsFactory,
        )
        player.initSight(mobWorldAdapter)
    }

    fun playerCanSleep(): Boolean {
        return mobs.none { mob ->
            mob.params.behaviorType == MobBehaviorType.AGGRESSIVE &&
                mob.position.cpy().sub(player.position).len() < 16f
        }
    }

    override fun dispose() {
        mobs.forEach { it.dispose() }
        player.dispose()
        _mobs.clear()
        mobSoundManager.dispose()
    }

    companion object {
        private const val SURVIVAL_CURSOR_RANGE_2 = 36f
    }
}
