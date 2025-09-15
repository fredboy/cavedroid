package ru.fredboy.cavedroid.data.items.repository

import com.badlogic.gdx.Gdx
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import ru.fredboy.cavedroid.common.utils.removeFirst
import ru.fredboy.cavedroid.data.items.mapper.BlockMapper
import ru.fredboy.cavedroid.data.items.mapper.ItemMapper
import ru.fredboy.cavedroid.data.items.model.BlockDto
import ru.fredboy.cavedroid.data.items.model.CraftingDto
import ru.fredboy.cavedroid.data.items.model.DropAmountDto
import ru.fredboy.cavedroid.data.items.model.GameItemsDto
import ru.fredboy.cavedroid.data.items.model.ItemDto
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.craft.CraftingEntry
import ru.fredboy.cavedroid.domain.items.model.craft.CraftingRecipe
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ItemsRepositoryImpl @Inject constructor(
    private val itemMapper: ItemMapper,
    private val blockMapper: BlockMapper,
) : ItemsRepository {

    private var _initialized: Boolean = false

    private val blocksMap = LinkedHashMap<String, Block>()
    private val itemsMap = LinkedHashMap<String, Item>()
    private val craftingRecipes = LinkedList<CraftingEntry>()

    override lateinit var fallbackBlock: Block.None
        private set

    override lateinit var fallbackItem: Item.None
        private set

    init {
        initialize()
    }

    private fun loadBlocks(dtoMap: Map<String, BlockDto>) {
        dtoMap.forEach { (key, dto) ->
            blocksMap[key] = blockMapper.map(key, dto)
                .apply(Block::initialize)
        }

        fallbackBlock = blocksMap[FALLBACK_BLOCK_KEY] as? Block.None
            ?: throw IllegalArgumentException("Fallback block key '$FALLBACK_BLOCK_KEY' not found")
    }

    private fun loadItems(dtoMap: Map<String, ItemDto>) {
        if (dtoMap.isNotEmpty() && blocksMap.isEmpty()) {
            throw IllegalStateException("items should be loaded after blocks")
        }

        dtoMap.forEach { (key, dto) ->
            try {
                itemsMap[key] = itemMapper.map(
                    key = key,
                    dto = dto,
                    block = blocksMap[key],
                    slabTopBlock = blocksMap[dto.topSlabBlock] as? Block.Slab,
                    slabBottomBlock = blocksMap[dto.bottomSlabBlock] as? Block.Slab,
                )
            } catch (e: Exception) {
                Gdx.app.error(TAG, "Failed to map item $key. Reason: ${e.message}")
                e.printStackTrace()
            }
        }

        fallbackItem = itemsMap[FALLBACK_ITEM_KEY] as? Item.None
            ?: throw IllegalArgumentException("Fallback item key '$FALLBACK_ITEM_KEY' not found")
    }

    private fun loadCraftingRecipes() {
        val jsonString = Gdx.files.internal("json/crafting.json").readString()
        val jsonMap = JsonFormat.decodeFromString<Map<String, CraftingDto>>(jsonString)

        if (jsonMap.isNotEmpty() && itemsMap.isEmpty()) {
            throw IllegalStateException("items should be loaded before crafting")
        }

        jsonMap.forEach { (key, value) ->
            craftingRecipes += CraftingEntry(
                recipes = value.recipes.map { recipe ->
                    CraftingRecipe(
                        input = recipe.input.map { it?.let(::Regex) },
                        isShapeless = recipe.shapeless,
                        amount = recipe.count,
                    )
                },
                result = getItemByKey(key),
            )
        }
    }

    private fun <T> Map<String, T>.getOrFallback(key: String, fallback: T, lazyErrorMessage: () -> String): T {
        if (!_initialized) {
            throw IllegalStateException("GameItemsHolder was not initialized before use")
        }

        val t = this[key] ?: run {
            Gdx.app.error(TAG, lazyErrorMessage.invoke())
            return fallback
        }
        return t
    }

    override fun initialize() {
        if (_initialized) {
            Gdx.app.debug(TAG, "Attempted to init when already initialized")
            return
        }

        val jsonString = Gdx.files.internal("json/game_items.json").readString()
        val gameItemsDto = JsonFormat.decodeFromString<GameItemsDto>(jsonString)

        loadBlocks(gameItemsDto.blocks)
        loadItems(gameItemsDto.items)

        _initialized = true

        loadCraftingRecipes()
    }

    override fun getItemByKey(key: String): Item = itemsMap.getOrFallback(key, fallbackItem) {
        "No item with key '$key' found. Returning $FALLBACK_ITEM_KEY"
    }

    override fun getItemByIndex(index: Int): Item = if (index in itemsMap.values.indices) {
        itemsMap.values.elementAt(index)
    } else {
        fallbackItem
    }

    override fun getBlockByKey(key: String): Block = blocksMap.getOrFallback(key, fallbackBlock) {
        "No block with key '$key' found. Returning $FALLBACK_BLOCK_KEY"
    }

    override fun <T : Block> getBlocksByType(type: Class<T>): List<T> = blocksMap.values.filterIsInstance(type)

    private fun mirrorPattern(pattern: List<Regex?>): List<Regex?> {
        return pattern.chunked(3)
            .map { it.reversed() }
            .flatten()
    }

    private fun patternMatches(input: List<Item>, pattern: List<Regex?>): Boolean {
        val inputHeight = input.chunked(3)
            .dropWhile { it.all { item -> item.isNone() } }
            .dropLastWhile { it.all { item -> item.isNone() } }
            .size

        val inputWidth = input.asSequence()
            .chunked(3)
            .map { row ->
                row.dropWhile { it.isNone() }
                    .dropLastWhile { it.isNone() }
            }
            .maxOf { row -> row.size }

        var droppedInput = 0
        val input = input.dropWhile {
            if (droppedInput < (3 - inputHeight) * 3 + (3 - inputWidth) && it.isNone()) {
                droppedInput++
                true
            } else {
                false
            }
        }

        val patternHeight = pattern.chunked(3)
            .dropWhile { it.all { item -> item == null } }
            .dropLastWhile { it.all { item -> item == null } }
            .size
        val patternWidth = pattern.asSequence()
            .chunked(3)
            .map { row ->
                row.dropWhile { it == null }
                    .dropLastWhile { it == null }
            }
            .maxOf { row -> row.size }

        var droppedPattern = 0
        val pattern = pattern.dropWhile {
            if (droppedPattern < (3 - patternHeight) * 3 + (3 - patternWidth) && it == null) {
                droppedPattern++
                true
            } else {
                false
            }
        }

        if (droppedInput % 3 > 3 - patternWidth) {
            return false
        }

        for (i in pattern.indices) {
            val inputItem = input.getOrNull(i) ?: fallbackItem
            val patternItem = pattern[i]

            if (patternItem?.let { regex -> !inputItem.params.key.matches(regex) } ?: !inputItem.isNone()) {
                return false
            }
        }
        return true
    }

    private fun shapelessMatches(input: List<Item>, pattern: List<Regex?>): Boolean {
        val inputKeys = input.filter { !it.isNone() }.map { it.params.key }.toMutableList()
        val patternKeys = pattern.filterNotNull()
        if (inputKeys.size != patternKeys.size) return false
        for (key in patternKeys) {
            if (!inputKeys.removeFirst { it.matches(key) }) return false
        }
        return inputKeys.isEmpty()
    }

    override fun getCraftingResult(input: List<InventoryItem>): InventoryItem {
        val inputItems = input.map { it.item }
        val inputTotalDurability = input.sumOf { if (it.item is Item.Durable) it.durability else 0 }

        for (entry in craftingRecipes) {
            for (recipe in entry.recipes) {
                if (recipe.isShapeless) {
                    if (shapelessMatches(inputItems, recipe.input)) {
                        return entry.result.toInventoryItem(recipe.amount, inputTotalDurability)
                    }
                } else {
                    if (patternMatches(inputItems, recipe.input) ||
                        patternMatches(
                            input = inputItems,
                            pattern = mirrorPattern(recipe.input),
                        )
                    ) {
                        return entry.result.toInventoryItem(recipe.amount, inputTotalDurability)
                    }
                }
            }
        }

        return inputItems.filter { it !is Item.None }
            .takeIf { it.size >= 2 && it.all { item -> item is Item.Durable && item == it.first() } }
            ?.first()?.toInventoryItem(durability = inputTotalDurability)
            ?: fallbackItem.toInventoryItem()
    }

    override fun getAllItems(): Collection<Item> = itemsMap.values

    override fun getAllCraftingRecipes(): Collection<CraftingEntry> = craftingRecipes

    override fun dispose() {
        _initialized = false

        blocksMap.clear()
        itemsMap.clear()
        craftingRecipes.clear()
    }

    override fun reload() {
        dispose()
        initialize()
    }

    companion object {
        private const val TAG = "ItemsRepositoryImpl"

        private val JsonFormat = Json {
            serializersModule = SerializersModule {
                polymorphic(DropAmountDto::class) {
                    subclass(DropAmountDto.ExactAmount::class)
                    subclass(DropAmountDto.RandomRange::class)
                    subclass(DropAmountDto.RandomChance::class)
                }
            }
            ignoreUnknownKeys = true
            classDiscriminator = "type"
        }

        const val FALLBACK_BLOCK_KEY = "none"
        const val FALLBACK_ITEM_KEY = "none"
    }
}
