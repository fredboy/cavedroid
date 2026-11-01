package ru.fredboy.cavedroid.game.window.inventory

import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem.Companion.isNoneOrNull
import ru.fredboy.cavedroid.domain.items.model.inventory.SafeInventoryItemMutableList
import ru.fredboy.cavedroid.domain.items.model.item.CreativeCategory
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs

class CreativeInventoryTabsWindow(
    private val itemsRepository: ItemsRepository,
) : AbstractInventoryWindowWithScroll() {

    override val type = GameWindowType.CREATIVE_INVENTORY_TABS

    override var selectedItem: InventoryItem? = null

    var selectedTab: Tab = Tab.Building

    fun getCatalogItems(): List<Item> = itemsRepository.getAllItems()
        .filter { selectedTab.isRelevantItem(it) }
        .sortedBy { it.params.key }

    override fun getMaxScroll(): Int {
        val count = itemsRepository.getAllItems().count { selectedTab.isRelevantItem(it) }
        val columns = GameWindowsConfigs.CreativeTabs.itemsInRow
        return ((count + columns - 1) / columns - GameWindowsConfigs.CreativeTabs.itemsInCol).coerceAtLeast(0)
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

    enum class Tab(
        val category: CreativeCategory?,
        val iconKey: String,
        val localizationKey: String,
    ) {
        Building(CreativeCategory.Building, "bricks", "creative_category_building"),
        Nature(CreativeCategory.Nature, "grass", "creative_category_nature"),
        Utilities(CreativeCategory.Utilities, "crafting_table", "creative_category_utilities"),
        Equipment(CreativeCategory.Equipment, "iron_pickaxe", "creative_category_equipment"),
        Food(CreativeCategory.Food, "wheat", "creative_category_food"),
        Materials(CreativeCategory.Materials, "iron_ingot", "creative_category_materials"),
        Creatures(CreativeCategory.Creatures, "spawn_egg_pig", "creative_category_creatures"),
        Inventory(null, "chest", "window_inventory"),
        ;

        val isInventory: Boolean get() = this == Inventory

        fun isRelevantItem(item: Item): Boolean {
            return item !is Item.None && (isInventory || item.params.creativeCategory == category)
        }
    }
}
