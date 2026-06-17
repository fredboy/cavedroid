package ru.fredboy.cavedroid.game.controller.fire

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.world.lighting.LightHandle
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.lighting.LightingSystem

class FireControllerTest {

    private fun stubBlock(combustible: Boolean): Block {
        val params = mockk<CommonBlockParams>()
        every { params.combustible } returns combustible
        return mockk {
            every { this@mockk.params } returns params
            every { this@mockk.isNone() } returns false
            every { this@mockk.isFire() } returns false
        }
    }

    private fun setup(
        worldWidth: Int = 512,
        foreground: Block = stubBlock(combustible = true),
        background: Block = stubBlock(combustible = false),
    ): Triple<FireController, GameWorld, LightingSystem> {
        val gameWorld = mockk<GameWorld>(relaxed = true)
        val lighting = mockk<LightingSystem>(relaxed = true)
        every { gameWorld.width } returns worldWidth
        every { gameWorld.getForeMap(any(), any()) } returns foreground
        every { gameWorld.getBackMap(any(), any()) } returns background
        every { lighting.createFireLight(any(), any()) } returns mockk<LightHandle>(relaxed = true)
        every { gameWorld.addBlockDestroyedListener(any()) } just Runs
        every { gameWorld.removeBlockDestroyedListener(any()) } just Runs
        return Triple(FireController(gameWorld, lighting, mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true)), gameWorld, lighting)
    }

    @Test
    fun `addFire FOREGROUND succeeds on combustible foreground`() {
        val (controller, _, lighting) = setup()
        val instance = controller.addFire(5, 10, Layer.FOREGROUND)
        assertNotNull(instance)
        assertEquals(Layer.FOREGROUND, instance!!.layer)
        assertTrue(controller.hasFireAt(5, 10, Layer.FOREGROUND))
        verify(exactly = 1) { lighting.createFireLight(5.5f, 10.5f) }
    }

    @Test
    fun `addFire BACKGROUND succeeds on combustible background`() {
        val (controller, _, _) = setup(
            foreground = stubBlock(combustible = false),
            background = stubBlock(combustible = true),
        )
        val instance = controller.addFire(3, 4, Layer.BACKGROUND)
        assertNotNull(instance)
        assertEquals(Layer.BACKGROUND, instance!!.layer)
        assertTrue(controller.hasFireAt(3, 4, Layer.BACKGROUND))
    }

    @Test
    fun `ignite picks foreground when both layers are combustible`() {
        val (controller, _, _) = setup(
            foreground = stubBlock(combustible = true),
            background = stubBlock(combustible = true),
        )
        val instance = controller.ignite(2, 2)
        assertEquals(Layer.FOREGROUND, instance?.layer)
        assertTrue(controller.hasFireAt(2, 2, Layer.FOREGROUND))
        assertFalse(controller.hasFireAt(2, 2, Layer.BACKGROUND))
    }

    @Test
    fun `ignite falls back to background when foreground is not combustible`() {
        val (controller, _, _) = setup(
            foreground = stubBlock(combustible = false),
            background = stubBlock(combustible = true),
        )
        val instance = controller.ignite(2, 2)
        assertEquals(Layer.BACKGROUND, instance?.layer)
    }

    @Test
    fun `ignite returns null when neither layer is combustible`() {
        val (controller, _, _) = setup(
            foreground = stubBlock(combustible = false),
            background = stubBlock(combustible = false),
        )
        assertNull(controller.ignite(2, 2))
        assertFalse(controller.hasAnyFireAt(2, 2))
    }

    @Test
    fun `addFire is idempotent per layer but allows both layers at the same cell`() {
        val (controller, _, lighting) = setup(
            foreground = stubBlock(combustible = true),
            background = stubBlock(combustible = true),
        )
        assertNotNull(controller.addFire(1, 1, Layer.FOREGROUND))
        assertNull(controller.addFire(1, 1, Layer.FOREGROUND))
        assertNotNull(controller.addFire(1, 1, Layer.BACKGROUND))
        assertEquals(2, controller.size)
        verify(exactly = 2) { lighting.createFireLight(any(), any()) }
    }

    @Test
    fun `removeFire disposes the light handle and decrements size`() {
        val handle = mockk<LightHandle>(relaxed = true)
        val gameWorld = mockk<GameWorld>(relaxed = true)
        val lighting = mockk<LightingSystem>(relaxed = true)
        every { gameWorld.width } returns 64
        every { gameWorld.getForeMap(any(), any()) } returns stubBlock(combustible = true)
        every { gameWorld.getBackMap(any(), any()) } returns stubBlock(combustible = false)
        every { lighting.createFireLight(any(), any()) } returns handle
        every { handle.dispose() } just Runs

        val controller = FireController(gameWorld, lighting, mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true))
        controller.addFire(7, 8, Layer.FOREGROUND)
        controller.removeFire(7, 8, Layer.FOREGROUND)

        assertFalse(controller.hasFireAt(7, 8, Layer.FOREGROUND))
        assertEquals(0, controller.size)
        verify(exactly = 1) { handle.dispose() }
    }

    @Test
    fun `onBlockDestroyed only clears fire on the destroyed layer`() {
        val listenerSlot = slot<OnBlockDestroyedListener>()
        val gameWorld = mockk<GameWorld>(relaxed = true)
        val lighting = mockk<LightingSystem>(relaxed = true)
        every { gameWorld.width } returns 64
        every { gameWorld.getForeMap(any(), any()) } returns stubBlock(combustible = true)
        every { gameWorld.getBackMap(any(), any()) } returns stubBlock(combustible = true)
        every { lighting.createFireLight(any(), any()) } returns mockk<LightHandle>(relaxed = true)
        every { gameWorld.addBlockDestroyedListener(capture(listenerSlot)) } just Runs

        val controller = FireController(gameWorld, lighting, mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true))
        controller.addFire(2, 3, Layer.FOREGROUND)
        controller.addFire(2, 3, Layer.BACKGROUND)

        val anyBlock = stubBlock(combustible = true)
        listenerSlot.captured.onBlockDestroyed(anyBlock, 2, 3, Layer.BACKGROUND, withDrop = false, destroyedByPlayer = false)
        assertTrue(controller.hasFireAt(2, 3, Layer.FOREGROUND))
        assertFalse(controller.hasFireAt(2, 3, Layer.BACKGROUND))

        listenerSlot.captured.onBlockDestroyed(anyBlock, 2, 3, Layer.FOREGROUND, withDrop = false, destroyedByPlayer = false)
        assertFalse(controller.hasFireAt(2, 3, Layer.FOREGROUND))
    }

    @Test
    fun `dispose clears all fires and removes the listener`() {
        val (controller, gameWorld, _) = setup()
        controller.addFire(1, 1, Layer.FOREGROUND)
        controller.addFire(2, 2, Layer.FOREGROUND)
        assertEquals(2, controller.size)

        controller.dispose()
        assertEquals(0, controller.size)
        verify(exactly = 1) { gameWorld.removeBlockDestroyedListener(controller) }
    }

    @Test
    fun `hasFireAt handles negative x by wrapping around world width`() {
        val (controller, _, _) = setup(worldWidth = 100)
        controller.addFire(5, 10, Layer.FOREGROUND)
        assertTrue(controller.hasFireAt(5, 10, Layer.FOREGROUND))
        assertTrue(controller.hasFireAt(-95, 10, Layer.FOREGROUND))
        assertTrue(controller.hasFireAt(105, 10, Layer.FOREGROUND))
    }
}
