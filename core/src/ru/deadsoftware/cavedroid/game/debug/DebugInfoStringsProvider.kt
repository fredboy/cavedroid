package ru.deadsoftware.cavedroid.game.debug

import com.badlogic.gdx.Gdx
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.objects.DropController
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class DebugInfoStringsProvider @Inject constructor(
    private val mobsController: MobsController,
    private val dropController: DropController,
    private val gameWorld: GameWorld
) {

    fun getDebugStrings(): List<String> {
        val player = mobsController.player

        return listOf(
            "FPS: ${Gdx.graphics.framesPerSecond}",
            "X: ${player.mapX}",
            "Y: ${gameWorld.height - player.upperMapY}",
            "CurX: ${player.cursorX}",
            "CurY: ${player.cursorY}",
            "Velocity: ${player.velocity}",
            "Swim: ${player.swim}",
            "Mobs: ${mobsController.mobs.size}",
            "Drops: ${dropController.size}",
            "Block: ${gameWorld.getForeMap(player.cursorX, player.cursorY).params.key}",
            "Hand: ${player.inventory.activeItem.item.params.key}",
            "Game mode: ${player.gameMode}",
            "Block damage: ${player.blockDamage}"
        )
    }
}