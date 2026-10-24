package ru.fredboy.cavedroid.gameplay.controls.usecase

import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.domain.assets.repository.FoodSoundAssetsRepository
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.entity.mob.model.Player
import javax.inject.Inject

class UseFoodInteractor @Inject constructor(
    private val soundPlayer: SoundPlayer,
    private val foodSoundAssetsRepository: FoodSoundAssetsRepository,
    private val plantCropInteractor: PlantCropInteractor,
) {

    /**
     * @return true if the food was planted or eaten
     */
    fun execute(player: Player): Boolean {
        val food = player.activeItem.item as? Item.Food ?: return false
        val plantBlockKey = food.plantBlockKey
        if (plantBlockKey != null &&
            plantCropInteractor.execute(player, plantBlockKey, player.selectedX, player.selectedY)
        ) {
            return true
        }

        if (player.foodLevel >= Player.MAX_FOOD_LEVEL) {
            return false
        }

        playFoodSound(player)
        player.eat(food.heal, food.saturation)
        player.decreaseCurrentItemCount()

        return true
    }

    private fun playFoodSound(player: Player) {
        val sound = foodSoundAssetsRepository.getFoodSound() ?: return

        soundPlayer.playSoundAtPosition(
            sound = sound,
            soundX = player.position.x,
            soundY = player.position.y,
            playerX = player.position.x,
            playerY = player.position.y,
        )
    }
}
