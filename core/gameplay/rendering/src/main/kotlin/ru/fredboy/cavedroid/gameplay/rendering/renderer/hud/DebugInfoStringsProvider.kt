package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.floorToInt
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.projectile.ProjectileController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.abstraction.GameWorldSolidBlockBodiesManager
import ru.fredboy.cavedroid.game.world.generator.ChunkGenerator
import ru.fredboy.cavedroid.game.world.store.InfiniteBlockStore
import javax.inject.Inject

@GameScope
class DebugInfoStringsProvider @Inject constructor(
    private val mobController: MobController,
    private val dropController: DropController,
    private val containerController: ContainerController,
    private val gameWorld: GameWorld,
    private val gameWorldSolidBlockBodiesManager: GameWorldSolidBlockBodiesManager,
    private val projectileController: ProjectileController,
) {

    fun getDebugStrings(): List<String> {
        val player = mobController.player

        return listOf(
            "FPS: ${Gdx.graphics.framesPerSecond}",
            "X: ${player.position.x} (Chunk: ${player.position.x.floorToInt() / ChunkGenerator.CHUNK_W})",
            "Y: ${player.position.y} (${gameWorld.height.toFloat() - player.position.y}) " +
                    "(Chunk: ${player.position.y.floorToInt() / ChunkGenerator.CHUNK_W})",
            "Cursor: (${player.cursorX}, ${player.cursorY})",
            "Aim: (${player.aimX}, ${player.aimY})",
            "Velocity: ${player.velocity}",
            "Control: ${player.controlVector}",
            "Mobs: ${mobController.mobs.size}",
            "Drops: ${dropController.size}",
            "Containers: ${containerController.size}",
            "Block: ${gameWorld.getForeMap(player.selectedX, player.selectedY).params.key}",
            "Hand: ${player.activeItem.item.params.key}",
            "Game mode: ${player.gameMode}",
            "Block damage: ${player.blockDamage}",
            "Player can climb: ${player.canClimb}",
            "Player climb: ${player.climb}",
            "Player tryClimb: ${player.tryClimb}",
            "Player can jump: ${player.canJump}",
            "Player fly mode: ${player.isFlyMode}",
            "Static bodies: ${gameWorldSolidBlockBodiesManager.bodies.size}",
            "Fixtures: ${gameWorld.world.fixtureCount}",
            "Projectiles: ${projectileController.projectiles.count()}",
        )
    }
}
