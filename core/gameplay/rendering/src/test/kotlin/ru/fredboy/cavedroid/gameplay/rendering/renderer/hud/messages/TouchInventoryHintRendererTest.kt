package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.messages

import com.badlogic.gdx.math.Rectangle
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.utils.TutorialTapTargets
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager

class TouchInventoryHintRendererTest {

    @Test
    fun `Scene2D window clears inventory tap target without advancing inventory tutorial`() {
        val app = mockk<ApplicationContextRepository> {
            every { isTouch() } returns true
            every { isInventoryHintShown() } returns false
        }
        val windows = mockk<GameWindowsManager> {
            every { currentWindowType } returns GameWindowType.SCENE2D
            every { currentWindow } returns null
        }
        val hints = InventoryHintController(app).apply { notifyItemMoved() }
        val targets = TutorialTapTargets().apply {
            register("touchInventory", Rectangle(0f, 0f, 10f, 10f)) { true }
        }
        val renderer = TouchInventoryHintRenderer(app, windows, mockk(), hints, targets, mockk(), mockk(), mockk())

        renderer.draw(mockk(), mockk(), Rectangle(0f, 0f, 640f, 320f), 3f)

        assertEquals(0, hints.hintIndex)
        assertFalse(targets.dismissAt(5f, 5f))
    }
}
