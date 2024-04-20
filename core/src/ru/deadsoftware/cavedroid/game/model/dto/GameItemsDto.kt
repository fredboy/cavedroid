package ru.deadsoftware.cavedroid.game.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class GameItemsDto(
    @SerialName("blocks") val blocks: Map<String, BlockDto>,
    @SerialName("items") val items: Map<String, ItemDto>,
) {
    object GameItemsDtoJsonSerializer : JsonTransformingSerializer<GameItemsDto>(GameItemsDto.serializer()) {
        private val defaultBlockValuesEqualKeyFieldNames = listOf("drop", "texture")
        private val defaultItemValuesEqualKeyFieldNames = listOf("name", "texture")

        override fun transformDeserialize(element: JsonElement): JsonElement {
            val root = element.jsonObject
            val blocks = root["blocks"]!!.jsonObject
            val items = root["items"]!!.jsonObject

            return buildJsonObject {
                putJsonObject("blocks") {
                    blocks.forEach { (key, blockObj) ->
                        putJsonObject(key) {
                            defaultBlockValuesEqualKeyFieldNames.forEach { fieldName ->
                                put(fieldName, key)
                            }
                            blockObj.jsonObject.forEach { (prop, propValue) ->
                                put(prop, propValue)
                            }
                        }
                    }
                }

                putJsonObject("items") {
                    items.forEach { (key, itemObj) ->
                        putJsonObject(key) {
                            defaultItemValuesEqualKeyFieldNames.forEach { fieldName ->
                                put(fieldName, key)
                            }
                            itemObj.jsonObject.forEach { (prop, propValue) ->
                                put(prop, propValue)
                            }
                        }
                    }
                }
            }
        }
    }
}