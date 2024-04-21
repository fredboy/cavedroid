package ru.deadsoftware.cavedroid.game.debug

import com.badlogic.gdx.Gdx
import ru.deadsoftware.cavedroid.game.GameInput
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.objects.DropController
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class DebugInfoStringsProvider @Inject constructor(
    private val mobsController: MobsController,
    private val gameInput: GameInput,
    private val dropController: DropController,
    private val gameWorld: GameWorld
) {

    fun getDebugStrings(): List<String> {
        val player = mobsController.player

        return listOf(
            "FPS: ${Gdx.graphics.framesPerSecond}",
            "X: ${player.mapX}",
            "Y: ${player.upperMapY}",
            "CurX: ${gameInput.curX}",
            "CurY: ${gameInput.curY}",
            "Velocity: ${player.velocity}",
            "Swim: ${player.swim}",
            "Mobs: ${mobsController.mobs.size}",
            "Drops: ${dropController.size}",
            "Block: ${gameWorld.getForeMap(gameInput.curX, gameInput.curY).params.key}",
            "Hand: ${player.inventory[player.slot].item.params.key}",
            "Game mode: ${player.gameMode}"
        )
    }
}