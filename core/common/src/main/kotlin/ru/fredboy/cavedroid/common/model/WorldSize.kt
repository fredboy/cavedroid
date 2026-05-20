package ru.fredboy.cavedroid.common.model

enum class WorldSize(val blocks: Int) {
    TINY(256),
    SMALL(512),
    NORMAL(1024),
    LARGE(2048),
    ;

    companion object {
        val DEFAULT = NORMAL
    }
}
