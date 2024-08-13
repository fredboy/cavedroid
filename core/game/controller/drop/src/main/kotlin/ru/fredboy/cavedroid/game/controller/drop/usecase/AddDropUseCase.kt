package ru.fredboy.cavedroid.game.controller.drop.usecase

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.drop.model.Drop
import ru.fredboy.cavedroid.domain.items.model.item.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import javax.inject.Inject

@GameScope
class AddDropUseCase @Inject constructor(
    private val dropController: DropController,
) {

    operator fun invoke(drop: Drop) {
        dropController.addDrop(drop)
    }

    operator fun invoke(x: Float, y: Float, item: Item, count: Int) {
        dropController.addDrop(x, y, item, count)
    }

    operator fun invoke(x: Float, y: Float, inventoryItem: InventoryItem) {
        dropController.addDrop(x, y, inventoryItem)
    }

}