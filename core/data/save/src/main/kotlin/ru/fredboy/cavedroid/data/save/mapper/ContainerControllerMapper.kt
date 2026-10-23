package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerWorldAdapter
import ru.fredboy.cavedroid.entity.container.abstraction.MobSpawnerAdapter
import ru.fredboy.cavedroid.entity.container.model.Chest
import ru.fredboy.cavedroid.entity.container.model.Container
import ru.fredboy.cavedroid.entity.container.model.ContainerCoordinates
import ru.fredboy.cavedroid.entity.container.model.Furnace
import ru.fredboy.cavedroid.entity.container.model.Spawner
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import javax.inject.Inject

@Reusable
class ContainerControllerMapper @Inject constructor(
    private val chestMapper: ChestMapper,
    private val spawnerMapper: SpawnerMapper,
    private val furnaceMapper: FurnaceMapper,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
) {

    fun mapSaveData(containerController: ContainerController): SaveDataDto.ContainerControllerSaveDataDto = SaveDataDto.ContainerControllerSaveDataDto(
        version = SAVE_DATA_VERSION,
        containerMap = containerController.containerMap.mapNotNull { (key, container) ->
            mapContainerSaveData(container)?.let { value -> key.toString() to value }
        }.toMap(),
    )

    fun mapEmptySaveData(): SaveDataDto.ContainerControllerSaveDataDto = SaveDataDto.ContainerControllerSaveDataDto(
        version = SAVE_DATA_VERSION,
        containerMap = emptyMap(),
    )

    fun mapContainerSaveData(container: Container): SaveDataDto.ContainerSaveDataDto? = when (container) {
        is Furnace -> furnaceMapper.mapSaveData(container)
        is Chest -> chestMapper.mapSaveData(container)
        is Spawner -> spawnerMapper.mapSaveData(container)
        else -> null
    }

    fun mapContainer(saveDataDto: SaveDataDto.ContainerSaveDataDto): Container? = when (saveDataDto) {
        is SaveDataDto.FurnaceSaveDataDto -> furnaceMapper.mapFurnace(saveDataDto)
        is SaveDataDto.ChestSaveDataDto -> chestMapper.mapChest(saveDataDto)
        is SaveDataDto.SpawnerSaveDataDto -> spawnerMapper.mapSpawner(saveDataDto)
        else -> null
    }

    fun mapContainerController(
        saveDataDto: SaveDataDto.ContainerControllerSaveDataDto,
        containerWorldAdapter: ContainerWorldAdapter,
        containerFactory: ContainerFactory,
        dropAdapter: DropAdapter,
        mobSpawnerAdapter: MobSpawnerAdapter,
    ): ContainerController {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return ContainerController(
            getItemByKeyUseCase = getItemByKeyUseCase,
            containerWorldAdapter = containerWorldAdapter,
            containerFactory = containerFactory,
            dropAdapter = dropAdapter,
            mobSpawnerAdapter = mobSpawnerAdapter,
        ).apply {
            saveDataDto.containerMap.forEach { (key, value) ->
                val container = mapContainer(value)

                if (container != null) {
                    val coordinates = ContainerCoordinates.fromString(key)
                    containerMap[coordinates] = container
                    onContainerAdded(coordinates.x, coordinates.y, coordinates.z, container)
                }
            }
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 3
    }
}
