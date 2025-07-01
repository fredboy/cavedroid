package ru.fredboy.cavedroid.domain.items.usecase

import dagger.Reusable
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import javax.inject.Inject

@Reusable
class GetBlockByKeyUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository,
) {

    operator fun get(key: String): Block {
        return itemsRepository.getBlockByKey(key)
    }
}
