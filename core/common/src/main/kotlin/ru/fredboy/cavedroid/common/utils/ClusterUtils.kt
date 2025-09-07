package ru.fredboy.cavedroid.common.utils

fun neighbourCoordinates(x: Int, y: Int, chunkSize: Int): List<Pair<Int, Int>> {
    val chunkX1 = x - x % chunkSize
    val chunkY1 = y - y % chunkSize
    val chunkX2 = chunkX1 + chunkSize
    val chunkY2 = chunkY1 + chunkSize

    val chunkHorizontal = chunkX1 until chunkX2
    val chunkVertical = chunkY1 until chunkY2

    return listOf(
        (x - 1) to y,
        (x + 1) to y,
        x to y - 1,
        x to y + 1,
    ).filter { (x, y) -> x in chunkHorizontal && y in chunkVertical }
}
