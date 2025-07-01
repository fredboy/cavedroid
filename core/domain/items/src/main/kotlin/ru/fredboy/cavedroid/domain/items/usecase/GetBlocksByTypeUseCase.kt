package ru.fredboy.cavedroid.domain.items.usecase

import dagger.Reusable
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import javax.inject.Inject

@Reusable
class GetBlocksByTypeUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository,
) {

    operator fun <T : Block> get(type: Class<T>): List<T> = itemsRepository.getBlocksByType(type)
}
