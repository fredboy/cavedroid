package ru.fredboy.cavedroid.gameplay.controls.action.placeblock

import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.domain.assets.repository.StepsSoundAssetsRepository
import ru.fredboy.cavedroid.domain.items.model.block.FenceBlocks
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.model.item.isSlab
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.domain.stats.repository.StatsRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.vehicle.VehicleController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindPlaceBlockAction
import javax.inject.Inject

@GameScope
@BindPlaceBlockAction(stringKey = PlaceBlockItemToForegroundAction.ACTION_KEY)
class PlaceBlockItemToForegroundAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val placeSlabAction: PlaceSlabAction,
    private val mobController: MobController,
    private val stepsSoundAssetsRepository: StepsSoundAssetsRepository,
    private val soundPlayer: SoundPlayer,
    private val statsRepository: StatsRepository,
    private val getBlockByKey: GetBlockByKeyUseCase,
    private val vehicles: VehicleController,
) : IPlaceBlockAction {

    override fun place(placeable: Item.Placeable, x: Int, y: Int): Boolean {
        if (placeable.isSlab()) {
            return placeSlabAction.place(placeable, x, y)
        }

        val block = placeable.block.takeIf(FenceBlocks::isFence)?.let { fence ->
            getBlockByKey[FenceBlocks.connectedKey(fence, gameWorld.getForeMap(x - 1, y), gameWorld.getForeMap(x + 1, y))]
        } ?: placeable.block
        val blockRect by lazy { block.getRectangle(x, y) }
        if (block.params.hasCollision &&
            (
                mobController.player.hitbox.overlaps(blockRect) ||
                    mobController.mobs.any { mob -> mob.hitbox.overlaps(blockRect) } ||
                    vehicles.overlapsVehicle(blockRect)
                )
        ) {
            return false
        }

        val attachToBottom = gameWorld.getForeMap(x, y + 1).params.hasCollision
        val attachToNeighbour = placeable.block.params.allowAttachToNeighbour &&
            (
                gameWorld.getForeMap(x - 1, y).params.run { hasCollision && !isTransparent } ||
                    gameWorld.getForeMap(x + 1, y).params.run { hasCollision && !isTransparent } ||
                    gameWorld.getBackMap(x, y).params.hasCollision
                )

        if (placeable.block.params.requiresBlock && !attachToBottom && !attachToNeighbour) {
            return false
        }

        if (placeable.block.params.requiresBackground &&
            !gameWorld.getBackMap(x, y).params.run { hasCollision && isFullBlock }
        ) {
            return false
        }

        return if (gameWorld.placeToForeground(x, y, block)) {
            mobController.player.decreaseCurrentItemCount()
            statsRepository.recordBlockPlaced()
            true
        } else {
            false
        }.apply {
            ifTrue {
                placeable.block.params.material?.name?.lowercase()?.also { material ->
                    val sound = stepsSoundAssetsRepository.getStepSound(material) ?: return@also
                    soundPlayer.playSoundAtPosition(
                        sound = sound,
                        soundX = x.toFloat(),
                        soundY = y.toFloat(),
                        playerX = mobController.player.position.x,
                        playerY = mobController.player.position.y,
                    )
                }
            }
        }
    }

    companion object {
        const val ACTION_KEY = "place_foreground_block"
    }
}
