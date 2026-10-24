package ru.fredboy.cavedroid.gameplay.controls.usecase

import ru.fredboy.cavedroid.domain.items.model.item.isNone
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

class PlantCropInteractor @Inject constructor(
    private val gameWorld: GameWorld,
    private val itemsRepository: ItemsRepository,
) {

    fun execute(player: Player, blockKey: String, x: Int, y: Int): Boolean {
        val plantY = when {
            gameWorld.getForeMap(x, y).isNone() &&
                gameWorld.getForeMap(x, y + 1).params.key in FARMLAND_KEYS -> y
            gameWorld.getForeMap(x, y).params.key in FARMLAND_KEYS &&
                gameWorld.getForeMap(x, y - 1).isNone() -> y - 1
            else -> return false
        }

        gameWorld.setForeMap(x, plantY, itemsRepository.getBlockByKey(blockKey))
        player.decreaseCurrentItemCount()
        return true
    }

    companion object {
        private val FARMLAND_KEYS = setOf("farmland", "farmland_moist")
    }
}
