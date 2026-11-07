package ru.fredboy.cavedroid.game.controller.waystone

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.domain.world.waystone.Waystone
import ru.fredboy.cavedroid.domain.world.waystone.WaystoneTravelResult
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.vehicle.VehicleController
import ru.fredboy.cavedroid.game.world.GameWorld

class WaystoneTravelTest {
    private var infinite = true
    private var entries = emptyList<Waystone>()
    private val blocks = mutableMapOf<Triple<Int, Int, Layer>, Block>()
    private fun block(key: String, solid: Boolean = true, damage: Int = 0) = mockk<Block.Normal>(relaxed = true) {
        every { params.key } returns key
        every { params.hasCollision } returns solid
        every { params.damage } returns damage
        every { getRectangle(any(), any()) } answers { Rectangle(firstArg<Int>().toFloat(), secondArg<Int>().toFloat(), 1f, 1f) }
    }
    private val air = block("none", false)
    private val pad = block("waystone")
    private val playerPosition = Vector2(.5f, 63.08f)
    private val player = mockk<Player>(relaxed = true) {
        every { position } returns playerPosition
        every { health } returns 20
        every { width } returns .7f
        every { height } returns 1.8f
        every { gameMode } returns GameMode.SURVIVAL
    }
    private val world = mockk<GameWorld>(relaxed = true) {
        every { waystones } answers { entries }
        every { waystones = any() } answers { entries = firstArg() }
        every { isInfinite } answers { infinite }
        every { width } returns 100
        every { height } returns 256
        every { isChunkLoadedAt(any()) } returns true
        every { containerX(any()) } answers { if (infinite) firstArg() else Math.floorMod(firstArg(), 100) }
        every { getForeMap(any(), any()) } answers { blocks[Triple(firstArg(), secondArg(), Layer.FOREGROUND)] ?: air }
        every { getBackMap(any(), any()) } answers { blocks[Triple(firstArg(), secondArg(), Layer.BACKGROUND)] ?: air }
    }
    private val mobs = mockk<MobController>(relaxed = true) { every { this@mockk.player } returns this@WaystoneTravelTest.player }
    private val vehicles = mockk<VehicleController>(relaxed = true) {
        every { clear(any(), ignorePlayer = true) } answers {
            val bounds = firstArg<Rectangle>()
            bounds.y >= 0 && bounds.y + bounds.height <= 256 && blocks.none { (position, block) ->
                position.third == Layer.FOREGROUND && block.params.hasCollision && block.getRectangle(position.first, position.second).overlaps(bounds)
            }
        }
    }
    private val camera = CameraContext(Rectangle(), Rectangle(0f, 0f, 40f, 20f))
    private val context = mockk<GameContextRepository> { every { getCameraContext() } returns camera }
    private val travel = WaystoneTravelImpl(world, mobs, vehicles, context)

    private fun activate(x: Int, y: Int = 64, name: String = "Base"): Waystone {
        blocks[Triple(x, y, Layer.FOREGROUND)] = pad
        return requireNotNull(travel.activate(x, y, name))
    }

    @Test
    fun `only activated pads become destinations and saved names survive reopening`() {
        val source = activate(0, name = "Home")
        val destination = activate(-100000)
        blocks[Triple(2000, 64, Layer.FOREGROUND)] = pad
        assertTrue(travel.rename(destination.id, "Deep Mine"))
        assertEquals(listOf("Deep Mine"), travel.destinations(source.id).map { it.name })
        assertEquals("Deep Mine", travel.activate(-100000, 64, "Default")!!.name)
        assertEquals(2, entries.size)
        assertEquals(WaystoneTravelResult.DESTINATION_UNAVAILABLE, travel.teleport(source.id, "0:2000:64"))
    }

    @Test
    fun `long distance travel prepares collision before relocating and resets camera presentation`() {
        val source = activate(0)
        val destination = activate(-100000)
        assertEquals(WaystoneTravelResult.SUCCESS, travel.teleport(source.id, destination.id))
        verifyOrder {
            world.ensureTerrainLoaded(-100003, -99997)
            world.prepareArrival(any(), any())
            mobs.flushPendingSpawns(any())
            player.teleportTo(-99999.5f, match { kotlin.math.abs(it - 63.08f) < .001f })
            world.onPlayerTeleported()
        }
        assertEquals(-99999.5f, camera.visibleWorld.x + camera.visibleWorld.width / 2f)
    }

    @Test
    fun `unloaded destinations remain listed and are loaded before checking whether they still exist`() {
        val source = activate(0)
        val destination = activate(100000)
        var loaded = false
        every { world.isChunkLoadedAt(100000) } answers { loaded }
        every { world.getForeMap(100000, 64) } answers { if (loaded) pad else air }
        every { world.ensureTerrainLoaded(any(), any()) } answers { loaded = true }
        assertEquals(listOf(destination), travel.destinations(source.id))
        assertEquals(WaystoneTravelResult.SUCCESS, travel.teleport(source.id, destination.id))
    }

    @Test
    fun `blocked or hazardous arrivals leave the player in place`() {
        val source = activate(0)
        val destination = activate(1000)
        every { vehicles.clear(any(), ignorePlayer = true) } returns false
        assertEquals(WaystoneTravelResult.DESTINATION_BLOCKED, travel.teleport(source.id, destination.id))
        verify(exactly = 0) { player.teleportTo(any(), any()) }
        assertTrue(entries.any { it.id == destination.id })
    }

    @Test
    fun `dangerous support and liquids cannot be used as arrival ground`() {
        val source = activate(0)
        val destination = activate(1000)
        val lava = block("lava", false, 4)
        every { lava.isFluid() } returns true
        for (dx in -2..2) {
            for (y in 60..66) {
                if (dx != 0 || y != 64) blocks[Triple(1000 + dx, y, Layer.FOREGROUND)] = lava
            }
        }
        assertEquals(WaystoneTravelResult.DESTINATION_BLOCKED, travel.teleport(source.id, destination.id))
        verify(exactly = 0) { player.teleportTo(any(), any()) }
    }

    @Test
    fun `removed and undiscovered destinations cannot be used and removals keep layers independent`() {
        val source = activate(0)
        val destination = activate(1000)
        blocks.remove(Triple(1000, 64, Layer.FOREGROUND))
        assertEquals(WaystoneTravelResult.DESTINATION_UNAVAILABLE, travel.teleport(source.id, destination.id))
        assertFalse(entries.any { it.id == destination.id })
        val background = source.copy(layer = Layer.BACKGROUND)
        entries = entries + background
        travel.onBlockDestroyed(pad, 0, 64, Layer.FOREGROUND, true, true)
        assertEquals(listOf(background), entries)
    }

    @Test
    fun `background waystones arrive on nearby foreground ground`() {
        val source = activate(0)
        blocks[Triple(1000, 64, Layer.BACKGROUND)] = pad
        blocks[Triple(1000, 65, Layer.FOREGROUND)] = block("stone")
        val destination = requireNotNull(travel.activate(1000, 64, "Cave"))
        assertEquals(Layer.BACKGROUND, destination.layer)
        assertEquals(WaystoneTravelResult.SUCCESS, travel.teleport(source.id, destination.id))
        verify { player.teleportTo(1000.5f, match { kotlin.math.abs(it - 64.08f) < .001f }) }
    }

    @Test
    fun `looping worlds normalize aliases and sort destinations across the seam`() {
        infinite = false
        val source = activate(0)
        val far = activate(50)
        val seam = activate(99)
        assertNotNull(travel.activate(-1, 64, "Alias"))
        assertEquals(3, entries.size)
        assertEquals(listOf(seam, far), travel.destinations(source.id))
        assertEquals(1L, travel.distance(source, seam))
    }
}
