package ru.fredboy.cavedroid.data.items.repository

import com.badlogic.gdx.Gdx
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import ru.fredboy.cavedroid.data.items.mapper.BlockMapper
import ru.fredboy.cavedroid.data.items.mapper.ItemMapper
import ru.fredboy.cavedroid.data.items.model.BlockDto
import ru.fredboy.cavedroid.data.items.model.CraftingDto
import ru.fredboy.cavedroid.data.items.model.DropAmountDto
import ru.fredboy.cavedroid.data.items.model.GameItemsDto
import ru.fredboy.cavedroid.data.items.model.ItemDto
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.craft.CraftingRecipe
import ru.fredboy.cavedroid.domain.items.model.craft.CraftingResult
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.LinkedHashMap

@Singleton
internal class ItemsRepositoryImpl @Inject constructor(
    private val itemMapper: ItemMapper,
    private val blockMapper: BlockMapper,
) : ItemsRepository {

    private var _initialized: Boolean = false

    private val blocksMap = LinkedHashMap<String, Block>()
    private val itemsMap = LinkedHashMap<String, Item>()
    private val craftingRecipes = LinkedList<CraftingRecipe>()

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
            craftingRecipes += CraftingRecipe(
                input = value.input.map(::Regex),
                output = CraftingResult(getItemByKey(key), value.count),
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

    override fun getCraftingResult(input: List<Item>): InventoryItem {
        val startIndex = input.indexOfFirst { !it.isNone() }.takeIf { it >= 0 }
            ?: return fallbackItem.toInventoryItem()

        val output = craftingRecipes.firstOrNull { rec ->
            for (i in rec.input.indices) {
                if (startIndex + i >= input.size) {
                    return@firstOrNull rec.input.subList(i, rec.input.size).all { it.matches("none") }
                }
                if (!input[startIndex + i].params.key.matches(rec.input[i])) {
                    return@firstOrNull false
                }
            }
            return@firstOrNull true
        }?.output

        return output?.toInventoryItem() ?: fallbackItem.toInventoryItem()
    }

    override fun getAllItems(): Collection<Item> = itemsMap.values

    override fun dispose() {
        _initialized = false

        blocksMap.clear()
        itemsMap.clear()
        craftingRecipes.clear()
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
