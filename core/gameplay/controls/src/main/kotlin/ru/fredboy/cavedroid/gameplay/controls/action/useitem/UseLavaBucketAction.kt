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
@BindUseItemAction(UseLavaBucketAction.ACTION_KEY)
class UseLavaBucketAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
    private val stepsSoundAssetsRepository: StepsSoundAssetsRepository,
    private val soundPlayer: SoundPlayer,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int): Boolean {
        val lava = getBlockByKeyUseCase["lava"]

        if (!gameWorld.canPlaceToForeground(x, y, lava)) {
            return false
        }

        gameWorld.placeToForeground(x, y, lava)

        lava.params.material?.name?.lowercase()
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
        const val ACTION_KEY = "use_lava_bucket"
    }
}
