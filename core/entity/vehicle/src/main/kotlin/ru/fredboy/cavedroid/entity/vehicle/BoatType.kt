package ru.fredboy.cavedroid.entity.vehicle

enum class BoatType(val itemKey: String) {
    OAK("oak_boat"),
    BIRCH("birch_boat"),
    SPRUCE("spruce_boat"),
    ;

    companion object {
        fun fromItemKey(key: String): BoatType? = entries.firstOrNull { it.itemKey == key }
    }
}
