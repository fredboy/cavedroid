package ru.fredboy.cavedroid.entity.container.model

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.entity.container.abstraction.MobSpawnerAdapter

class SpawnerTest {
    private val adapter = mockk<MobSpawnerAdapter>(relaxed = true)
    private val coordinates = ContainerCoordinates(-14, 180, 0)
    private val spawner = Spawner(mockk(), "spider")

    @Test
    fun `inactive spawner pauses its countdown`() {
        spawner.update(30f, coordinates, adapter)
        assertEquals(5f, spawner.remainingSeconds)
        verify(exactly = 0) { adapter.trySpawn(any(), any()) }
    }

    @Test
    fun `active spawner waits then attempts one bounded wave`() {
        every { adapter.isPlayerNearby(coordinates) } returns true
        every { adapter.trySpawn(coordinates, "spider") } returns true
        spawner.update(4f, coordinates, adapter)
        verify(exactly = 0) { adapter.trySpawn(any(), any()) }
        spawner.update(1f, coordinates, adapter)
        verify(exactly = 3) { adapter.trySpawn(coordinates, "spider") }
        assertTrue(spawner.remainingSeconds in 10f..20f)
    }

    @Test
    fun `failed spawn retries after a delay with the configured mob key`() {
        every { adapter.isPlayerNearby(coordinates) } returns true
        spawner.mobKey = "cow"
        spawner.update(5f, coordinates, adapter)
        assertEquals(2f, spawner.remainingSeconds)
        spawner.update(1f, coordinates, adapter)
        verify(exactly = 3) { adapter.trySpawn(coordinates, "cow") }
    }

    @Test
    fun `restored countdown does not restart on first update`() {
        every { adapter.isPlayerNearby(coordinates) } returns true
        val restored = Spawner(mockk(), "skeleton", remainingSeconds = 12f)
        restored.update(1f, coordinates, adapter)
        assertEquals(11f, restored.remainingSeconds)
        assertEquals(0, restored.inventory.size)
        verify(exactly = 0) { adapter.trySpawn(any(), any()) }
    }
}
