package ru.fredboy.cavedroid.game.window.inventory

import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem.Companion.isNoneOrNull
import ru.fredboy.cavedroid.domain.items.model.inventory.SafeInventoryItemMutableList
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs
import kotlin.reflect.KClass

class CreativeInventoryTabsWindow(
    private val itemsRepository: ItemsRepository,
) : AbstractInventoryWindowWithScroll() {

    override val type = GameWindowType.CREATIVE_INVENTORY_TABS

    override var selectedItem: InventoryItem? = null

    var selectedTab: Tab = Tab.Placeable

    override fun getMaxScroll(): Int {
        return itemsRepository.getAllItems().count { selectedTab.isRelevantItem(it) } /
            GameWindowsConfigs.CreativeTabs.itemsInRow
    }

    override fun onLeftCLick(
        items: MutableList<InventoryItem>,
        itemsRepository: ItemsRepository,
        index: Int,
        pointer: Int,
    ) {
        val clickedItem = items[index]

        selectedItem.takeIf { !it.isNoneOrNull() }?.let { selectedItem ->
            if (!clickedItem.isNoneOrNull() && !selectedTab.isInventory && items is SafeInventoryItemMutableList) {
                if (selectedItem.item == clickedItem.item) {
                    if (selectedItem.amount < selectedItem.item.params.maxStack) {
                        selectedItem.amount++
                    }
                } else {
                    this.selectedItem = null
                }
                return
            }
        }

        super.onLeftCLick(items, itemsRepository, index, pointer)
    }

    enum class Tab(val itemClass: KClass<out Item>, val isInventory: Boolean = false) {
        Placeable(Item.Placeable::class),
        Normal(Item.Normal::class),
        Tool(Item.Tool::class),
        Armor(Item.Armor::class),
        Usable(Item.Usable::class),
        Food(Item.Food::class),
        Inventory(Item::class, true),
        ;

        fun isRelevantItem(item: Item): Boolean {
            return itemClass.isInstance(item)
        }
    }
}
