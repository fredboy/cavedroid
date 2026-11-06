package ru.fredboy.cavedroid.game.world.store

interface PreparedChunkFlush {
    fun write()
    fun finish() {}
}
