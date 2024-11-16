package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.drop.model.Drop
import javax.inject.Inject

@Reusable
class DropMapper @Inject constructor(
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
) {

    fun mapSaveData(drop: Drop): SaveDataDto.DropSaveDataDto {
        return SaveDataDto.DropSaveDataDto(
            version = SAVE_DATA_VERSION,
            x = drop.x,
            y = drop.y,
            width = drop.width,
            height = drop.height,
            velocityX = drop.velocity.x,
            velocityY = drop.velocity.y,
            itemKey = drop.item.params.key,
            amount = drop.amount,
            pickedUp = drop.isPickedUp
        )
    }

    fun mapDrop(saveDataDto: SaveDataDto.DropSaveDataDto): Drop {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return Drop(
            x = saveDataDto.x,
            y = saveDataDto.y,
            item = getItemByKeyUseCase[saveDataDto.itemKey],
            amount = saveDataDto.amount
        ).apply {
            width = saveDataDto.width
            height = saveDataDto.height
            velocity.y = saveDataDto.velocityY
            velocity.x = saveDataDto.velocityX
            isPickedUp = saveDataDto.pickedUp
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 3
    }

}