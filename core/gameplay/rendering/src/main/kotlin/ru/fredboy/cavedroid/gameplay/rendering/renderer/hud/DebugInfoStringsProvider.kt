package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.abstraction.GameWorldSolidBlockBodiesManager
import javax.inject.Inject

@GameScope
class DebugInfoStringsProvider @Inject constructor(
    private val mobController: MobController,
    private val dropController: DropController,
    private val containerController: ContainerController,
    private val gameWorld: GameWorld,
    private val gameWorldSolidBlockBodiesManager: GameWorldSolidBlockBodiesManager,
) {

    fun getDebugStrings(): List<String> {
        val player = mobController.player

        return listOf(
            "FPS: ${Gdx.graphics.framesPerSecond}",
            "X: ${player.position.x}",
            "Y: ${player.position.y} (${gameWorld.height.toFloat() - player.position.y})",
            "CurX: ${player.cursorX}",
            "CurY: ${player.cursorY}",
            "Velocity: ${player.velocity}",
            "Control: ${player.controlVector}",
            "Swim: ${player.swim}",
            "Mobs: ${mobController.mobs.size}",
            "Drops: ${dropController.size}",
            "Containers: ${containerController.size}",
            "Block: ${gameWorld.getForeMap(player.cursorX, player.cursorY).params.key}",
            "Hand: ${player.activeItem.item.params.key}",
            "Game mode: ${player.gameMode}",
            "Block damage: ${player.blockDamage}",
            "Player can jump: ${player.canJump}",
            "Player fly mode: ${player.isFlyMode}",
            "Static bodies: ${gameWorldSolidBlockBodiesManager.bodies.size}",
            "Fixtures: ${gameWorld.world.fixtureCount}",
        )
    }
}
