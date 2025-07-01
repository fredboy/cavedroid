package ru.fredboy.cavedroid.domain.items.usecase

import dagger.Reusable
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import javax.inject.Inject

@Reusable
class GetCraftingResultUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository,
) {

    operator fun get(input: List<Item>): InventoryItem = itemsRepository.getCraftingResult(input)
}
