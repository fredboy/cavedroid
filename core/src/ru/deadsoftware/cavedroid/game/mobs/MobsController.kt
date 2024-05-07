package ru.deadsoftware.cavedroid.game.mobs

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.player.Player
import ru.deadsoftware.cavedroid.game.ui.TooltipManager
import java.io.Serializable
import java.util.*
import javax.inject.Inject

@GameScope
class MobsController @Inject constructor(
    gameItemsHolder: GameItemsHolder,
    tooltipManager: TooltipManager,
) : Serializable {

    private val _mobs = LinkedList<Mob>()

    val player: Player =
        Player(gameItemsHolder, tooltipManager)

    val mobs: List<Mob>
        get() = _mobs

    fun addMob(mob: Mob) {
        _mobs.add(mob)
    }

    companion object {
        private const val TAG = "MobsController"
    }
}