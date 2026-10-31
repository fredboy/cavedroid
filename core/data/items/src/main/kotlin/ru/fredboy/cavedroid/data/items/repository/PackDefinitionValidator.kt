package ru.fredboy.cavedroid.data.items.repository

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import ru.fredboy.cavedroid.domain.assets.pack.AssetResolver
import javax.inject.Inject

/** Validate cross-file references before mappers can silently substitute fallback items. */
class PackDefinitionValidator @Inject constructor(private val resolver: AssetResolver) {
    fun validate() {
        if (!resolver.hasOverrides) return
        fun read(path: String) = Json.parseToJsonElement(resolver.handle(path).readString("UTF-8")).jsonObject
        val definitions = read("json/game_items.json")
        val blocks = definitions.getValue("blocks").jsonObject
        val items = definitions.getValue("items").jsonObject
        val mobs = read("json/mobs.json")
        val crafting = read("json/crafting.json")
        require("none" in blocks && "none" in items && "char" in mobs) { "Required none/char definitions are missing" }
        fun reference(owner: String, obj: JsonObject, field: String, targets: JsonObject) {
            val value = obj[field]?.takeUnless { it == JsonNull } ?: return
            val key = (value as? JsonPrimitive)?.contentOrNull
            require(key != null && key in targets) { "$owner.$field references missing definition: $value" }
        }
        blocks.forEach { (key, value) ->
            val obj = value.jsonObject
            reference(key, obj, "full_block", blocks)
            reference(key, obj, "other_part", blocks)
            reference(key, obj, "mob_key", mobs)
        }
        items.forEach { (key, value) ->
            val obj = value.jsonObject
            listOf("top_slab_block", "bottom_slab_block", "plant_block").forEach { reference(key, obj, it, blocks) }
            reference(key, obj, "smelt_product", items)
            reference(key, obj, "mob_key", mobs)
        }
        crafting.keys.forEach { require(it in items) { "Recipe references missing result item: $it" } }
    }
}
