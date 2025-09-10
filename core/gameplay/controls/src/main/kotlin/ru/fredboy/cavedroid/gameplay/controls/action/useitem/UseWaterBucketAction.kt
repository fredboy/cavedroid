package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.repository.StepsSoundAssetsRepository
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import javax.inject.Inject

@GameScope
@BindUseItemAction(UseWaterBucketAction.ACTION_KEY)
class UseWaterBucketAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
    private val stepsSoundAssetsRepository: StepsSoundAssetsRepository,
    private val soundPlayer: SoundPlayer,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int): Boolean {
        val water = getBlockByKeyUseCase["water"]

        if (!gameWorld.canPlaceToForeground(x, y, water)) {
            return false
        }

        gameWorld.placeToForeground(x, y, water)

        water.params.material?.name?.lowercase()
            ?.let { material -> stepsSoundAssetsRepository.getStepSound(material) }
            ?.let { sound ->
                soundPlayer.playSoundAtPosition(
                    sound = sound,
                    soundX = x.toFloat(),
                    soundY = y.toFloat(),
                    playerX = mobController.player.position.x,
                    playerY = mobController.player.position.y,
                )
            }

        if (!mobController.player.gameMode.isCreative()) {
            mobController.player.setCurrentInventorySlotItem(getItemByKeyUseCase["bucket_empty"])
        }
        return true
    }

    companion object {
        const val ACTION_KEY = "use_water_bucket"
    }
}
