package ru.fredboy.cavedroid.domain.items.usecase

import dagger.Reusable
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import javax.inject.Inject

@Reusable
class GetItemByIndexUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository,
) {

    operator fun get(index: Int): Item {
        return itemsRepository.getItemByIndex(index)
    }
}
