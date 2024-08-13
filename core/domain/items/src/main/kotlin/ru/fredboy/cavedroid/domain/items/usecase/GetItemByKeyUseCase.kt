package ru.fredboy.cavedroid.domain.items.usecase

import dagger.Reusable
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import javax.inject.Inject

@Reusable
class GetItemByKeyUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository,
) {

    operator fun get(key: String): Item {
        return itemsRepository.getItemByKey(key)
    }

}