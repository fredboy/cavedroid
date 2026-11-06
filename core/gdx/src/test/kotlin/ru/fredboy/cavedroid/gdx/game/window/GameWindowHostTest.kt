package ru.fredboy.cavedroid.gdx.game.window

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.game.window.GameWindowModel
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.gameplay.controls.GameInputProcessor

class GameWindowHostTest {
    private val previousInput = Gdx.input
    private val previousGraphics = Gdx.graphics
    private val windows = GameWindowsManager(mockk(relaxed = true), mockk(), mockk(relaxed = true), mockk(relaxed = true))
    private val application = mockk<ApplicationContextRepository> { every { isTouch() } returns false }
    private val gameplay = mockk<GameInputProcessor>(relaxed = true)
    private val stage = mockk<Stage>(relaxed = true)
    private val factory = mockk<GameWindowFactory>()
    private val model = object : GameWindowModel {
        override val key = "example"
    }
    private val view = mockk<GameWindowView>(relaxed = true)
    private lateinit var host: GameWindowHost

    @BeforeEach
    fun setup() {
        Gdx.input = mockk(relaxed = true)
        Gdx.graphics = mockk {
            every { width } returns 1280
            every { height } returns 720
        }
        every { factory.create(model, false, any()) } returns view
        host = GameWindowHost(windows, application, gameplay, mapOf("example" to factory), stage)
    }

    @AfterEach
    fun cleanup() {
        host.dispose()
        Gdx.input = previousInput
        Gdx.graphics = previousGraphics
    }

    @Test
    fun `factory attaches once per model and draw resumes without recreating actors`() {
        windows.openWindow(model)
        repeat(3) { host.render(.016f) }
        verify(exactly = 1) { factory.create(model, false, any()) }
        verify(exactly = 1) { stage.addActor(view.root) }
        verify(exactly = 1) { view.onShow(stage) }
        verify(exactly = 3) { stage.draw() }
        verify(exactly = 1) { gameplay.suspendForWindow() }
    }

    @Test
    fun `closing clears focus hides keyboard and disposes view while keeping the stage reusable`() {
        windows.openWindow(model)
        host.render(.016f)
        windows.closeWindow()
        host.render(.016f)
        verify(exactly = 1) { view.dispose() }
        verify { stage.cancelTouchFocus() }
        verify { stage.unfocusAll() }
        verify { Gdx.input.setOnscreenKeyboardVisible(false) }
        verify(exactly = 0) { stage.dispose() }
        verify(exactly = 2) { gameplay.suspendForWindow() }
    }

    @Test
    fun `session disposal closes the model and stage exactly once`() {
        windows.openWindow(model)
        host.render(.016f)
        host.dispose()
        host.dispose()
        verify(exactly = 1) { view.dispose() }
        verify(exactly = 1) { stage.dispose() }
    }
}
