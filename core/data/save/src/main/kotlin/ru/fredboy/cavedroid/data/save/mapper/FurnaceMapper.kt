package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.container.model.Furnace
import javax.inject.Inject

@Reusable
class FurnaceMapper @Inject constructor(
    private val inventoryItemMapper: InventoryItemMapper,
    private val getFallbackItem: GetFallbackItemUseCase,
    private val getItemByKey: GetItemByKeyUseCase,
) {

    fun mapSaveData(furnace: Furnace): SaveDataDto.FurnaceSaveDataDto = SaveDataDto.FurnaceSaveDataDto(
        version = SAVE_DATA_VERSION,
        size = furnace.size,
        currentFuelItemKey = furnace.currentFuelKey,
        items = furnace.items.map(inventoryItemMapper::mapSaveData),
        startBurnTimeMs = furnace.startBurnTimeMs,
        startSmeltTimeMs = furnace.smeltStarTimeMs,
        burnProgress = furnace.burnProgress,
        smeltProgress = furnace.smeltProgress,
    )

    fun mapFurnace(saveDataDto: SaveDataDto.FurnaceSaveDataDto): Furnace {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return Furnace(
            fallbackItem = getFallbackItem(),
            initialItems = saveDataDto.items.map(inventoryItemMapper::mapInventoryItem),
        ).apply {
            currentFuel = saveDataDto.currentFuelItemKey?.let(getItemByKey::get) ?: getFallbackItem()
            startBurnTimeMs = saveDataDto.startBurnTimeMs
            smeltStarTimeMs = saveDataDto.startSmeltTimeMs
            burnProgress = saveDataDto.burnProgress
            smeltProgress = saveDataDto.smeltProgress
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 3
    }
}
