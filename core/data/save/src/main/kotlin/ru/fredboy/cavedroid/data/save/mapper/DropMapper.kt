package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import ru.fredboy.cavedroid.entity.drop.model.Drop
import javax.inject.Inject

@Reusable
class DropMapper @Inject constructor(
    private val inventoryItemMapper: InventoryItemMapper,
) {

    fun mapSaveData(drop: Drop): SaveDataDto.DropSaveDataDto = SaveDataDto.DropSaveDataDto(
        version = SAVE_DATA_VERSION,
        x = drop.position.x,
        y = drop.position.y,
        width = Drop.DROP_SIZE,
        height = Drop.DROP_SIZE,
        velocityX = drop.velocity.x,
        velocityY = drop.velocity.y,
        pickedUp = drop.isPickedUp,
        item = inventoryItemMapper.mapSaveData(drop.inventoryItem),
    )

    fun mapDrop(
        saveDataDto: SaveDataDto.DropSaveDataDto,
        dropWorldAdapter: DropWorldAdapter,
    ): Drop {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return Drop(
            inventoryItem = inventoryItemMapper.mapInventoryItem(saveDataDto.item),
        ).apply {
            spawn(saveDataDto.x, saveDataDto.y, dropWorldAdapter.getBox2dWorld())
            velocity.y = saveDataDto.velocityY
            velocity.x = saveDataDto.velocityX
            isPickedUp = saveDataDto.pickedUp
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 4
    }
}
