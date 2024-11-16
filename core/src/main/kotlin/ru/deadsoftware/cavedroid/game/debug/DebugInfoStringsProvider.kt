package ru.deadsoftware.cavedroid.game.debug

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class DebugInfoStringsProvider @Inject constructor(
    private val mobController: MobController,
    private val dropController: DropController,
    private val containerController: ContainerController,
    private val gameWorld: GameWorld,
) {

    fun getDebugStrings(): List<String> {
        val player = mobController.player

        return listOf(
            "FPS: ${Gdx.graphics.framesPerSecond}",
            "X: ${player.mapX}",
            "Y: ${player.upperMapY} (${gameWorld.height - player.upperMapY})",
            "CurX: ${player.cursorX}",
            "CurY: ${player.cursorY}",
            "Velocity: ${player.velocity}",
            "Swim: ${player.swim}",
            "Mobs: ${mobController.mobs.size}",
            "Drops: ${dropController.size}",
            "Containers: ${containerController.size}",
            "Block: ${gameWorld.getForeMap(player.cursorX, player.cursorY).params.key}",
            "Hand: ${player.activeItem.item.params.key}",
            "Game mode: ${player.gameMode}",
            "Block damage: ${player.blockDamage}"
        )
    }
}