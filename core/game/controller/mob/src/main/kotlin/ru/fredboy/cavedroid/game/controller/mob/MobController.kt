package ru.fredboy.cavedroid.game.controller.mob

import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.repository.MobAssetsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.behavior.PlayerMobBehavior
import java.util.*
import javax.inject.Inject

@GameScope
class MobController @Inject constructor(
    mobAssetsRepository: MobAssetsRepository,
    getFallbackItemUseCase: GetFallbackItemUseCase,
    private val mobWorldAdapter: MobWorldAdapter,
) {

    private val _mobs = LinkedList<Mob>()

    val mobs: List<Mob> get() = _mobs

    var player = Player(
        sprite = mobAssetsRepository.getPlayerSprites(),
        getFallbackItem = getFallbackItemUseCase,
        x = 0f,
        y = 0f,
        behavior = PlayerMobBehavior()
    )

    init {
        respawnPlayer()
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
            mob.update(mobWorldAdapter, delta)
        }
        player.update(mobWorldAdapter, delta)
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
        player.respawn(player.spawnPoint ?: mobWorldAdapter.findSpawnPoint())
    }

    companion object {
        private const val SURVIVAL_CURSOR_RANGE = 4
    }
}