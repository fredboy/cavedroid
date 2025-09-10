package ru.fredboy.cavedroid.gameplay.controls.action.placeblock

import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.domain.assets.repository.StepsSoundAssetsRepository
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.game.controller.mob.MobController
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
) : IPlaceBlockAction {

    override fun place(placeable: Item.Placeable, x: Int, y: Int): Boolean {
        return if (placeable.isSlab()) {
            placeSlabAction.place(placeable, x, y)
        } else if (gameWorld.placeToForeground(x, y, placeable.block)) {
            mobController.player.decreaseCurrentItemCount()
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
