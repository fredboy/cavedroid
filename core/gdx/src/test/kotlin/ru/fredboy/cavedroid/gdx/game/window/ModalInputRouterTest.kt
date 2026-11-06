package ru.fredboy.cavedroid.gdx.game.window

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModalInputRouterTest {
    @Test
    fun `every unhandled modal event is consumed without reaching gameplay`() {
        val modal = mockk<InputProcessor>(relaxed = true)
        val game = mockk<InputProcessor>(relaxed = true)
        val router = ModalInputRouter({ true }, modal, game)
        assertTrue(router.keyDown(1))
        assertTrue(router.keyUp(1))
        assertTrue(router.keyTyped('e'))
        assertTrue(router.touchDown(4, 5, 0, 0))
        assertTrue(router.touchUp(4, 5, 0, 0))
        assertTrue(router.touchCancelled(4, 5, 0, 0))
        assertTrue(router.touchDragged(4, 5, 0))
        assertTrue(router.mouseMoved(4, 5))
        assertTrue(router.scrolled(0f, 1f))
        verify(exactly = 0) { game.keyDown(any()) }
        verify(exactly = 0) { game.keyUp(any()) }
        verify(exactly = 0) { game.keyTyped(any()) }
        verify(exactly = 0) { game.touchDown(any(), any(), any(), any()) }
        verify(exactly = 0) { game.touchUp(any(), any(), any(), any()) }
        verify(exactly = 0) { game.touchCancelled(any(), any(), any(), any()) }
        verify(exactly = 0) { game.touchDragged(any(), any(), any()) }
        verify(exactly = 0) { game.mouseMoved(any(), any()) }
        verify(exactly = 0) { game.scrolled(any(), any()) }
        verify { modal.keyTyped('e') }
    }

    @Test
    fun `closing a modal during release cannot replay that release into the world`() {
        var open = true
        val game = mockk<InputProcessor>(relaxed = true)
        val modal = object : InputAdapter() {
            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                open = false
                return false
            }
        }
        val router = ModalInputRouter({ open }, modal, game)
        assertTrue(router.touchUp(0, 0, 0, 0))
        verify(exactly = 0) { game.touchUp(any(), any(), any(), any()) }
        every { game.keyTyped('d') } returns true
        assertTrue(router.keyTyped('d'))
    }

    @Test
    fun `a gameplay gesture that opens a modal synchronizes before the next event`() {
        var open = false
        val modal = mockk<InputProcessor>(relaxed = true)
        val game = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                open = true
                return true
            }
        }
        val observed = mutableListOf<Boolean>()
        val router = ModalInputRouter({ open }, modal, game) { observed += open }
        router.touchDown(0, 0, 0, 0)
        assertEquals(listOf(false, true), observed)
        assertTrue(router.touchUp(0, 0, 0, 0))
        verify { modal.touchUp(0, 0, 0, 0) }
    }
}
