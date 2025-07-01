package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerWorldAdapter
import ru.fredboy.cavedroid.entity.container.model.Chest
import ru.fredboy.cavedroid.entity.container.model.ContainerCoordinates
import ru.fredboy.cavedroid.entity.container.model.Furnace
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import javax.inject.Inject

@Reusable
class ContainerControllerMapper @Inject constructor(
    private val chestMapper: ChestMapper,
    private val furnaceMapper: FurnaceMapper,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
) {

    fun mapSaveData(containerController: ContainerController): SaveDataDto.ContainerControllerSaveDataDto {
        return SaveDataDto.ContainerControllerSaveDataDto(
            version = SAVE_DATA_VERSION,
            containerMap = containerController.containerMap.mapNotNull { (key, container) ->
                when (container) {
                    is Furnace -> furnaceMapper.mapSaveData(container)
                    is Chest -> chestMapper.mapSaveData(container)
                    else -> null
                }?.let { value -> key.toString() to value }
            }.toMap(),
        )
    }

    fun mapContainerController(
        saveDataDto: SaveDataDto.ContainerControllerSaveDataDto,
        containerWorldAdapter: ContainerWorldAdapter,
        containerFactory: ContainerFactory,
        dropAdapter: DropAdapter,
    ): ContainerController {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return ContainerController(
            getItemByKeyUseCase = getItemByKeyUseCase,
            containerWorldAdapter = containerWorldAdapter,
            containerFactory = containerFactory,
            dropAdapter = dropAdapter,
        ).apply {
            saveDataDto.containerMap.forEach { (key, value) ->
                val container = when (value) {
                    is SaveDataDto.FurnaceSaveDataDto -> furnaceMapper.mapFurnace(value)
                    is SaveDataDto.ChestSaveDataDto -> chestMapper.mapChest(value)
                    else -> null
                }

                if (container != null) {
                    containerMap.put(ContainerCoordinates.fromString(key), container)
                }
            }
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 3
    }
}
