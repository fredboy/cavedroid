package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.entity.container.model.Chest
import javax.inject.Inject

@Reusable
class ChestMapper @Inject constructor(
    private val inventoryItemMapper: InventoryItemMapper,
    private val getFallbackItemUseCase: GetFallbackItemUseCase,
) {

    fun mapSaveData(chest: Chest): SaveDataDto.ChestSaveDataDto = SaveDataDto.ChestSaveDataDto(
        version = SAVE_DATA_VERSION,
        size = chest.size,
        items = chest.items.map(inventoryItemMapper::mapSaveData),
    )

    fun mapChest(saveDataDto: SaveDataDto.ChestSaveDataDto): Chest {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return Chest(
            fallbackItem = getFallbackItemUseCase(),
            initialItems = saveDataDto.items.map(inventoryItemMapper::mapInventoryItem),
        )
    }

    companion object {
        private const val SAVE_DATA_VERSION = 3
    }
}
