package ru.fredboy.cavedroid.game.controller.mob

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.entity.mob.model.Player
import java.util.*
import javax.inject.Inject

@GameScope
class MobController @Inject constructor(
    getFallbackItemUseCase: GetFallbackItemUseCase,
    mobParamsRepository: MobParamsRepository,
    private val mobWorldAdapter: MobWorldAdapter,
    private val mobPhysicsFactory: MobPhysicsFactory,
    private val dropQueue: DropQueue,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
) : Disposable {

    private val _mobs = LinkedList<Mob>()

    val mobs: List<Mob> get() = _mobs

    var player = Player(
        getFallbackItem = getFallbackItemUseCase,
        params = requireNotNull(mobParamsRepository.getMobParamsByKey("char")),
    )
        set(value) {
            field.dispose()
            field = value
            field.initSight(mobWorldAdapter)
        }

    init {
        respawnPlayer()
        player.initSight(mobWorldAdapter)
    }

    fun addMob(mob: Mob) {
        // TODO: Probably shouldn't add if already in the list
        _mobs.add(mob)
    }

    fun removeMob(mob: Mob) {
        _mobs.remove(mob)
    }

    fun update(delta: Float) {
        mobs.forEach { mob -> mob.update(mobWorldAdapter, delta) }
        _mobs.removeAll { mob ->
            mob.isDead.ifTrue {
                dropQueue.offerItems(mob.position.x, mob.position.y, mob.getDropItems(getItemByKeyUseCase))
                mob.dispose()
                true
            } ?: false
        }

        player.update(mobWorldAdapter, delta)
        if (player.isDead) {
            dropQueue.offerInventory(player.position.x, player.position.y, player.inventory)
            player.inventory.clear()
            player.dispose()
            player.respawn(
                spawnPoint = player.spawnPoint ?: mobWorldAdapter.findSpawnPoint(),
                mobPhysicsFactory = mobPhysicsFactory,
            )
        }
    }

    fun checkPlayerCursorBounds() {
        with(player) {
            if (gameMode == 0) {
                val minCursorX = mapX - SURVIVAL_CURSOR_RANGE
                val maxCursorX = mapX + SURVIVAL_CURSOR_RANGE
                val minCursorY = middleMapY - SURVIVAL_CURSOR_RANGE
                val maxCursorY = middleMapY + SURVIVAL_CURSOR_RANGE

                cursorX = MathUtils.clamp(cursorX, minCursorX, maxCursorX)
                cursorY = MathUtils.clamp(cursorY, minCursorY, maxCursorY)
            }

            cursorY = MathUtils.clamp(cursorY, 0, mobWorldAdapter.height)
        }
    }

    fun respawnPlayer() {
        player.respawn(
            spawnPoint = player.spawnPoint ?: mobWorldAdapter.findSpawnPoint(),
            mobPhysicsFactory = mobPhysicsFactory,
        )
    }

    override fun dispose() {
        mobs.forEach { it.dispose() }
        player.dispose()
        _mobs.clear()
    }

    companion object {
        private const val SURVIVAL_CURSOR_RANGE = 4
    }
}
