package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.entity.container.model.Spawner
import javax.inject.Inject

@Reusable
class SpawnerMapper @Inject constructor(
    private val getFallbackItemUseCase: GetFallbackItemUseCase,
) {
    fun mapSaveData(spawner: Spawner) = SaveDataDto.SpawnerSaveDataDto(
        version = 1,
        mobKey = spawner.mobKey,
        remainingSeconds = spawner.remainingSeconds,
    )

    fun mapSpawner(dto: SaveDataDto.SpawnerSaveDataDto): Spawner {
        dto.verifyVersion(1)
        return Spawner(getFallbackItemUseCase(), dto.mobKey, dto.remainingSeconds)
    }
}
