package ru.fredboy.cavedroid.game.world.impl

import com.badlogic.gdx.math.Vector2
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.generator.GeneratorTestItems

class WorldSpawnTest {
    private val items = GeneratorTestItems()
    private var savedSpawn: Vector2? = null
    private val world = mockk<GameWorld> {
        every { worldSpawn } answers { savedSpawn }
        every { worldSpawn = any() } answers { savedSpawn = firstArg() }
        every { isInfinite } returns true
        every { height } returns 256
        every { getForeMap(0, any()) } answers {
            items.getBlockByKey(if (secondArg<Int>() >= 60) "stone" else "none")
        }
    }
    private val adapter = WorldAdapterImpl(
        world,
        items,
        mockk<MobParamsRepository> { every { getMobParamsByKey("char") } returns null },
        mockk(),
        mockk(),
        mockk(),
    )

    @Test
    fun `legacy world initializes a stable spawn and callers cannot mutate it`() {
        val initial = adapter.findSpawnPoint()
        assertEquals(Vector2(0f, 59f), initial)
        initial.set(200f, 200f)
        every { world.getForeMap(0, any()) } returns items.getBlockByKey("none")
        assertEquals(Vector2(0f, 59f), adapter.findSpawnPoint())
        assertEquals(Vector2(0f, 59f), savedSpawn)
    }

    @Test
    fun `restored spawn survives terrain changes and is returned by copy`() {
        savedSpawn = Vector2(-42f, 80f)
        val spawn = adapter.findSpawnPoint()
        assertEquals(savedSpawn, spawn)
        spawn.setZero()
        assertEquals(Vector2(-42f, 80f), adapter.findSpawnPoint())
    }
}
