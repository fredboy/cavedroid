package ru.fredboy.cavedroid.common.utils

import com.badlogic.gdx.math.Rectangle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BlockAreaTest {
    @Test
    fun `block area iteration floors distant negative coordinates accurately`() {
        val visited = mutableSetOf<Pair<Int, Int>>()
        forEachBlockInArea(Rectangle(-99999.85f, 63.98f, .7f, .12f)) { x, y -> visited += x to y }
        assertEquals(setOf(-100000 to 63, -100000 to 64, -100000 to 65, -99999 to 63, -99999 to 64, -99999 to 65), visited)
    }
}
