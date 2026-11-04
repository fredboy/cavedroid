package ru.fredboy.cavedroid.domain.items.model.item

/** A creative catalog group, independent of an item's behavior or runtime type. */
enum class CreativeCategory(val key: String) {
    Building("building"),
    Nature("nature"),
    Utilities("utilities"),
    Equipment("equipment"),
    Food("food"),
    Materials("materials"),
    Creatures("creatures"),
}
