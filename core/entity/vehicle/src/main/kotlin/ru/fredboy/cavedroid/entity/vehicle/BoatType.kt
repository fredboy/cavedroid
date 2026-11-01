package ru.fredboy.cavedroid.entity.vehicle

enum class BoatType(val itemKey: String, val legacyItemKey: String) {
    OAK("boat_oak", "oak_boat"),
    BIRCH("boat_birch", "birch_boat"),
    SPRUCE("boat_spruce", "spruce_boat"),
    ;

    companion object {
        fun fromItemKey(key: String): BoatType? = entries.firstOrNull { it.itemKey == key || it.legacyItemKey == key }
    }
}
