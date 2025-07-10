package ru.fredboy.cavedroid.game.world.helper

class WaterMapManager {

    private val waterTiles = mutableSetOf<Pair<Int, Int>>()

    fun addWaterTile(x: Int, y: Int) {
        waterTiles.add(x to y)
    }

    fun removeWaterTile(x: Int, y: Int) {
        waterTiles.remove(x to y)
    }

    fun getWaterTiles(): Set<Pair<Int, Int>> = waterTiles

    fun clear() {
        waterTiles.clear()
    }
}
