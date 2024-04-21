package ru.deadsoftware.cavedroid.game

import com.badlogic.gdx.Gdx
import kotlinx.serialization.json.Json
import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.deadsoftware.cavedroid.game.model.dto.BlockDto
import ru.deadsoftware.cavedroid.game.model.dto.GameItemsDto
import ru.deadsoftware.cavedroid.game.model.dto.ItemDto
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.game.model.mapper.BlockMapper
import ru.deadsoftware.cavedroid.game.model.mapper.ItemMapper
import ru.deadsoftware.cavedroid.misc.utils.AssetLoader
import javax.inject.Inject

@GameScope
class GameItemsHolder @Inject constructor(
    private val assetLoader: AssetLoader,
    private val blockMapper: BlockMapper,
    private val itemMapper: ItemMapper,
) {

    private var _initialized: Boolean = false

    private val blocksMap = LinkedHashMap<String, Block>()
    private val itemsMap = LinkedHashMap<String, Item>()

    lateinit var fallbackBlock: Block
        private set
    lateinit var fallbackItem: Item
        private set

    init {
        initialize()
    }

    private fun loadBlocks(dtoMap: Map<String, BlockDto>) {
        dtoMap.forEach { (key, dto) ->
            blocksMap[key] = blockMapper.map(key, dto)
                .apply(Block::initialize)
        }

        fallbackBlock = blocksMap[FALLBACK_BLOCK_KEY]
            ?: throw IllegalArgumentException("Fallback block key '$FALLBACK_BLOCK_KEY' not found")
    }

    private fun loadItems(dtoMap: Map<String, ItemDto>) {
        if (dtoMap.isNotEmpty() && blocksMap.isEmpty()) {
            throw IllegalStateException("items should be loaded after blocks")
        }

        dtoMap.forEach { (key, dto) ->
            itemsMap[key] = itemMapper.map(key, dto, blocksMap[key])
        }

        fallbackItem = itemsMap[FALLBACK_ITEM_KEY]
            ?: throw IllegalArgumentException("Fallback item key '$FALLBACK_ITEM_KEY' not found")
    }

    fun initialize() {
        if (_initialized) {
            Gdx.app.debug(TAG, "Attempted to init when already initialized")
            return
        }

        val jsonString = assetLoader.getAssetHandle("json/game_items.json").readString()
        val gameItemsDto = JsonFormat.decodeFromString<GameItemsDto>(jsonString)

        loadBlocks(gameItemsDto.blocks)
        loadItems(gameItemsDto.items)

        _initialized = true
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

    fun getBlock(key: String): Block {
        return blocksMap.getOrFallback(key, fallbackBlock) {
            "No block with key '$key' found. Returning $FALLBACK_BLOCK_KEY"
        }
    }

    fun getItem(key: String): Item {
        return itemsMap.getOrFallback(key, fallbackItem) {
            "No item with key '$key' found. Returning $FALLBACK_BLOCK_KEY"
        }
    }

    fun getAllItems(): Collection<Item> {
        return itemsMap.values
    }

    fun getItemFromCreativeInventory(position: Int): Item {
        return if (position in itemsMap.values.indices) {
            itemsMap.values.elementAt(position)
        } else {
            fallbackItem
        }
    }

    fun getCreativeScrollAmount(): Int = itemsMap.size / 8

    fun <T : Block> getBlocksByType(type: Class<T>): List<T> {
        return blocksMap.values.filterIsInstance(type)
    }

    companion object {
        private const val TAG = "GameItemsHolder"

        private val JsonFormat = Json { ignoreUnknownKeys = true }

        const val FALLBACK_BLOCK_KEY = "none"
        const val FALLBACK_ITEM_KEY = "none"
    }
}