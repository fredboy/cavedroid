package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.messages

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository

class InventoryHintControllerTest {
    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `tapping advances all inventory tips and persists completion on both control modes`(touch: Boolean) {
        val context = mockk<ApplicationContextRepository>(relaxed = true) {
            every { isTouch() } returns touch
        }
        val controller = InventoryHintController(context)
        repeat(3) { index ->
            assertTrue(controller.isVisible)
            assertEquals(index, controller.hintIndex)
            controller.dismiss()
            controller.tick(1f)
            controller.dismiss()
            controller.tick(1f)
        }
        assertFalse(controller.isVisible)
        verify(exactly = 1) { context.setInventoryHintShown(true) }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `actions still advance the same sequence`(touch: Boolean) {
        val context = mockk<ApplicationContextRepository>(relaxed = true) {
            every { isTouch() } returns touch
        }
        val controller = InventoryHintController(context)
        controller.notifyItemMoved()
        controller.tick(2f)
        assertEquals(1, controller.hintIndex)
        controller.notifyItemPlacedByOne()
        controller.tick(2f)
        assertEquals(2, controller.hintIndex)
        controller.notifyItemHeld()
        controller.tick(2f)
        assertFalse(controller.isVisible)
    }
}
