package ru.fredboy.cavedroid.game.window.inventory

import ru.fredboy.cavedroid.domain.items.model.craft.CraftingRecipe
import ru.fredboy.cavedroid.domain.items.model.inventory.Inventory
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs.RecipeBook.pageSize

abstract class AbstractInventoryWindowWithCraftGrid(
    itemsRepository: ItemsRepository,
) : AbstractInventoryWindow() {

    abstract val gridSize: Int

    private val _items = Array(10) { itemsRepository.fallbackItem.toInventoryItem() }

    val items get() = _items.asList()

    val craftingItems get() = items.subList(0, 9) as MutableList<InventoryItem>

    val craftResultList get() = items.subList(9, 10) as MutableList<InventoryItem>

    var craftResult: InventoryItem
        get() = craftResultList[0]
        set(value) {
            craftResultList[0] = value
        }

    var recipeBookActive = false

    var recipeBookPage = 0

    var selectedRecipe = -1

    fun getAvailableCraftingRecipes(itemsRepository: ItemsRepository): Sequence<Pair<CraftingRecipe, Item>> {
        return itemsRepository.getAllCraftingRecipes()
            .asSequence()
            .mapNotNull { (recipes, result) ->
                val recipe = recipes.firstOrNull {
                    it.getWidth() <= gridSize && it.getHeight() <= gridSize
                } ?: return@mapNotNull null

                recipe to result
            }
    }

    fun getVisibleCraftingRecipes(itemsRepository: ItemsRepository): List<Pair<CraftingRecipe, Item>> {
        return getAvailableCraftingRecipes(itemsRepository)
            .drop(pageSize * recipeBookPage)
            .take(pageSize)
            .toList()
    }

    fun getPhantomRecipe(
        itemsRepository: ItemsRepository,
    ): List<InventoryItem> {
        val (recipe, _) = getVisibleCraftingRecipes(itemsRepository).getOrNull(selectedRecipe) ?: return emptyList()
        val allItems = itemsRepository.getAllItems()

        return recipe.input.map { regex ->
            if (regex == null) {
                itemsRepository.fallbackItem.toInventoryItem(0)
            } else {
                allItems.first { it.params.key.matches(regex) }.toInventoryItem(0)
            }
        }
    }

    fun clearCrafting(
        playerAdapter: PlayerAdapter,
        dropQueue: DropQueue,
        itemsRepository: ItemsRepository,
    ) {
        dropQueue.offerItems(playerAdapter.x, playerAdapter.y, ArrayList(craftingItems))
        for (i in craftingItems.indices) {
            craftingItems[i] = itemsRepository.fallbackItem.toInventoryItem()
        }
    }

    fun tryFillPhantomRecipe(
        playerAdapter: PlayerAdapter,
        dropQueue: DropQueue,
        inventory: Inventory,
        itemsRepository: ItemsRepository,
    ) {
        val (recipe, result) = getVisibleCraftingRecipes(itemsRepository).getOrNull(selectedRecipe) ?: return

        if (craftResult.item != result) {
            clearCrafting(playerAdapter, dropQueue, itemsRepository)
        }

        recipe.input.forEachIndexed { index, pattern ->
            if (pattern == null) {
                return@forEachIndexed
            }

            val itemIndex = inventory.items.indexOfFirst { it.item.params.key.matches(pattern) && it.amount > 0 }
                .takeIf { it >= 0 }
                ?: return@forEachIndexed

            if (craftingItems[index].item == inventory.items[itemIndex].item) {
                craftingItems[index].add(1)
            } else {
                craftingItems[index] = inventory.items[itemIndex].copy().apply { amount = 1 }
            }

            inventory.items[itemIndex].subtract()

            if (inventory.items[itemIndex].amount == 0) {
                inventory.items[itemIndex] = itemsRepository.fallbackItem.toInventoryItem()
            }
        }
    }
}
