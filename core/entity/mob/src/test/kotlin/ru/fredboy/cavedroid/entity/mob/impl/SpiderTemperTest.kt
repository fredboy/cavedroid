package ru.fredboy.cavedroid.entity.mob.impl

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SpiderTemperTest {

    private val temper = SpiderTemper()

    @Test
    fun `daylight spider remains neutral until provoked`() {
        assertFalse(temper.update(isDayTime = true, mobY = 127f, seaLevel = 128, isCreative = false, delta = 0f))
        temper.provoke()
        assertTrue(temper.update(isDayTime = true, mobY = 127f, seaLevel = 128, isCreative = false, delta = 0f))
    }

    @Test
    fun `night spider attacks and calms down at dawn`() {
        assertTrue(temper.update(isDayTime = false, mobY = 127f, seaLevel = 128, isCreative = false, delta = 0f))
        assertFalse(temper.update(isDayTime = true, mobY = 127f, seaLevel = 128, isCreative = false, delta = 0f))
    }

    @Test
    fun `spider below sea level attacks during daytime`() {
        assertTrue(temper.update(isDayTime = true, mobY = 129f, seaLevel = 128, isCreative = false, delta = 0f))
    }

    @Test
    fun `daytime aggression starts strictly below sea level and ends on ascent`() {
        assertFalse(temper.update(isDayTime = true, mobY = 128f, seaLevel = 128, isCreative = false, delta = 0f))
        assertTrue(temper.update(isDayTime = true, mobY = 128.01f, seaLevel = 128, isCreative = false, delta = 0f))
        assertFalse(temper.update(isDayTime = true, mobY = 127.99f, seaLevel = 128, isCreative = false, delta = 0f))
    }

    @Test
    fun `depth threshold uses the world sea level`() {
        assertFalse(temper.update(isDayTime = true, mobY = 100f, seaLevel = 110, isCreative = false, delta = 0f))
        assertTrue(temper.update(isDayTime = true, mobY = 100f, seaLevel = 90, isCreative = false, delta = 0f))
    }

    @Test
    fun `provocation lasts thirty seconds and repeated attacks refresh it`() {
        temper.provoke()
        assertTrue(temper.update(isDayTime = true, mobY = 127f, seaLevel = 128, isCreative = false, delta = 29f))
        temper.provoke()
        assertTrue(temper.update(isDayTime = true, mobY = 127f, seaLevel = 128, isCreative = false, delta = 29f))
        assertFalse(temper.update(isDayTime = true, mobY = 127f, seaLevel = 128, isCreative = false, delta = 1f))
    }

    @Test
    fun `night and underground aggression continue after provocation expires`() {
        temper.provoke()
        assertTrue(temper.update(isDayTime = false, mobY = 127f, seaLevel = 128, isCreative = false, delta = 31f))
        assertTrue(temper.update(isDayTime = true, mobY = 129f, seaLevel = 128, isCreative = false, delta = 31f))
    }

    @Test
    fun `creative player is safe even underground at night after an attack`() {
        temper.provoke()
        assertFalse(temper.update(isDayTime = false, mobY = 129f, seaLevel = 128, isCreative = true, delta = 0f))
    }

    @Test
    fun `restored anger survives daylight until its remaining duration expires`() {
        temper.angerRemainingSec = 2f
        assertTrue(temper.update(isDayTime = true, mobY = 127f, seaLevel = 128, isCreative = false, delta = 1f))
        assertFalse(temper.update(isDayTime = true, mobY = 127f, seaLevel = 128, isCreative = false, delta = 1f))
    }
}
