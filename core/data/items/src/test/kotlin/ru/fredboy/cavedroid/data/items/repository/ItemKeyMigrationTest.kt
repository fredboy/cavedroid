package ru.fredboy.cavedroid.data.items.repository

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.I18NBundle
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.data.items.mapper.BlockMapper
import ru.fredboy.cavedroid.data.items.mapper.DropInfoMapper
import ru.fredboy.cavedroid.data.items.mapper.ItemMapper
import ru.fredboy.cavedroid.domain.assets.pack.AssetResolver
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.usecase.GetBlockTextureUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetItemTextureUseCase
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.item.Item
import java.util.Properties

class ItemKeyMigrationTest {
    private fun resource(path: String) = FileHandle(java.io.File(requireNotNull(javaClass.classLoader.getResource(path)).toURI()))
    private fun json(path: String) = Json.parseToJsonElement(resource(path).readString("UTF-8")).jsonObject

    @Test
    fun `every historical key resolves to a canonical definition without duplicate catalog entries`() {
        val definitions = json("json/game_items.json")
        for ((kind, aliases) in listOf("items" to LegacyItemKeys.items, "blocks" to LegacyItemKeys.blocks)) {
            val canonical = definitions.getValue(kind).jsonObject
            aliases.forEach { (old, new) ->
                assertFalse(old in canonical, old)
                assertTrue(new in canonical, "$old -> $new")
                assertFalse(new in aliases, "Aliases must resolve directly to canonical keys: $new")
            }
            assertEquals(aliases.size, aliases.values.toSet().size)
        }
    }

    @Test
    fun `real mappers load every bundled definition and every historical key resolves to its canonical object`() {
        val resolver = mockk<AssetResolver> {
            every { hasOverrides } returns true
            every { handle(any()) } answers { resource(firstArg()) }
        }
        val textureCache = mutableMapOf<String, Texture>()
        fun texture(kind: String, name: String): Texture = textureCache.getOrPut("$kind/$name") {
            // Only mock the GL boundary; actual PNG dimensions drive real sprite construction.
            val png = java.nio.ByteBuffer.wrap(resource("textures/textures/$kind/$name.png").readBytes())
            mockk<Texture> {
                every { width } returns png.getInt(16)
                every { height } returns png.getInt(20)
            }
        }
        val blocks = mockk<GetBlockTextureUseCase> {
            every { this@mockk[any()] } answers { texture("blocks", firstArg()) }
        }
        val items = mockk<GetItemTextureUseCase> {
            every { this@mockk[any()] } answers { texture("items", firstArg()) }
        }
        val bundle = I18NBundle.createBundle(resource("i18n/CaveDroid_Items_en.properties").sibling("CaveDroid_Items"), java.util.Locale.ENGLISH)
        val fonts = mockk<FontTextureAssetsRepository> { every { getItemLocalizationBundle() } returns bundle }
        val repository = ItemsRepositoryImpl(resolver, ItemMapper(items, fonts, mockk()), BlockMapper(blocks, DropInfoMapper()))
        val definitions = json("json/game_items.json")
        val itemKeys = definitions.getValue("items").jsonObject.keys
        assertEquals(itemKeys, repository.getAllItems().map { it.params.key }.toSet())
        definitions.getValue("blocks").jsonObject.keys.forEach { key ->
            assertEquals(key, repository.getBlockByKey(key).params.key)
        }
        LegacyItemKeys.items.forEach { (old, new) ->
            assertSame(repository.getItemByKey(new), repository.getItemByKey(old), old)
        }
        LegacyItemKeys.blocks.forEach { (old, new) ->
            assertSame(repository.getBlockByKey(new), repository.getBlockByKey(old), old)
        }
        repository.getAllItems().filterNot { it is Item.None }.forEach { item ->
            assertTrue(item.sprite.width > 0f && item.sprite.height > 0f, item.params.key)
        }
    }

    @Test
    fun `saved keys use canonical objects and aliases do not add catalog items`() {
        val noneBlock = mockk<Block.None>(relaxed = true)
        val planks = mockk<Block.Normal>(relaxed = true)
        val noneItem = mockk<Item.None>()
        val pickaxe = mockk<Item.Normal>()
        val resolver = mockk<AssetResolver>()
        val definitions = mockk<FileHandle>()
        val recipes = mockk<FileHandle>()
        every { resolver.handle("json/game_items.json") } returns definitions
        every { resolver.handle("json/crafting.json") } returns recipes
        every { definitions.readString() } returns """
            {
              "blocks": {"none": {"texture": "none"}, "wood_planks_oak": {"texture": "wood_planks_oak"}},
              "items": {"none": {"name": "", "texture": "none", "type": "none"},
                        "tool_pickaxe_iron": {"name": "Iron Pickaxe", "texture": "tool_pickaxe_iron"}}
            }
        """.trimIndent()
        every { recipes.readString() } returns "{}"
        val blockMapper = mockk<BlockMapper>()
        every { blockMapper.map("none", any()) } returns noneBlock
        every { blockMapper.map("wood_planks_oak", any()) } returns planks
        val itemMapper = mockk<ItemMapper>()
        every { itemMapper.map("none", any(), any(), any(), any()) } returns noneItem
        every { itemMapper.map("tool_pickaxe_iron", any(), any(), any(), any()) } returns pickaxe
        val repository = ItemsRepositoryImpl(resolver, itemMapper, blockMapper)
        assertSame(repository.getItemByKey("tool_pickaxe_iron"), repository.getItemByKey("iron_pickaxe"))
        assertSame(pickaxe, repository.getItemByKey("iron_pickaxe"))
        assertSame(repository.getBlockByKey("wood_planks_oak"), repository.getBlockByKey("planks_oak"))
        assertEquals(2, repository.getAllItems().size)
        assertSame(pickaxe, repository.getItemByIndex(1))
    }

    @Test
    fun `bundled block item mob and crafting references point to canonical definitions`() {
        val resolver = mockk<AssetResolver> {
            every { hasOverrides } returns true
            every { handle(any()) } answers { resource(firstArg()) }
        }
        PackDefinitionValidator(resolver).validate()
        val definitions = json("json/game_items.json")
        val items = definitions.getValue("items").jsonObject
        val blocks = definitions.getValue("blocks").jsonObject
        items.forEach { (key, value) ->
            if (value.jsonObject["type"]?.jsonPrimitive?.content == "block") assertTrue(key in blocks, key)
        }
        fun checkDrops(value: kotlinx.serialization.json.JsonElement) {
            when (value) {
                is JsonObject -> value.forEach { (key, child) ->
                    if (key == "key" && child is JsonPrimitive) {
                        // These pre-existing unobtainable blocks have no inventory item definition.
                        val unobtainable = setOf("sponge", "noteblock")
                        assertTrue(child.content in items || child.content in unobtainable, "Drop: $child")
                    } else {
                        checkDrops(child)
                    }
                }
                is kotlinx.serialization.json.JsonArray -> value.forEach(::checkDrops)
                else -> Unit
            }
        }
        checkDrops(definitions)
        checkDrops(json("json/mobs.json"))
        json("json/crafting.json").forEach { (result, value) ->
            value.jsonObject.getValue("recipes").jsonArray.forEach { recipe ->
                recipe.jsonObject.getValue("input").jsonArray.forEach { input ->
                    if (input is JsonPrimitive && input.isString) {
                        val pattern = Regex(input.content)
                        assertTrue(items.keys.any(pattern::matches), "$result: $pattern matches no items")
                    }
                }
            }
        }
    }

    @Test
    fun `all item block and bow state textures exist and boats have key derived atlas regions`() {
        val definitions = json("json/game_items.json")
        fun assertTexture(kind: String, name: String) {
            assertTrue(javaClass.classLoader.getResource("textures/textures/$kind/$name.png") != null, "$kind/$name.png")
        }
        definitions.getValue("blocks").jsonObject.forEach { (key, value) ->
            if (key != "none") assertTexture("blocks", value.jsonObject.getValue("texture").jsonPrimitive.content)
        }
        val boatKeys = mutableListOf<String>()
        definitions.getValue("items").jsonObject.forEach { (key, value) ->
            val dto = value.jsonObject
            // Block and slab items derive their sprites from their block definitions.
            if (key != "none" && dto["type"]?.jsonPrimitive?.content !in setOf("block", "slab")) {
                assertTexture("items", dto.getValue("texture").jsonPrimitive.content)
            }
            dto["state_sprites"]?.jsonArray?.forEach { assertTexture("items", it.jsonPrimitive.content) }
            if (dto["action_key"]?.jsonPrimitive?.content == "use_boat") boatKeys += key
        }
        val regions = json("json/texture_regions.json").values.flatMap { it.jsonObject.keys }.toSet()
        boatKeys.forEach { key ->
            for (part in listOf("side", "rim", "oar", "blade")) assertTrue("${key}_$part" in regions, "${key}_$part")
        }
    }

    @Test
    fun `all locales name every canonical item and contain no historical translation keys`() {
        val items = json("json/game_items.json").getValue("items").jsonObject.keys - "none"
        for (locale in listOf("en", "ru", "de", "es", "pt")) {
            val translations = Properties().apply { resource("i18n/CaveDroid_Items_$locale.properties").reader("UTF-8").use(::load) }
            items.forEach { assertTrue(translations.containsKey(it), "$locale: $it") }
            LegacyItemKeys.items.keys.forEach { assertFalse(translations.containsKey(it), "$locale: $it") }
        }
    }
}
