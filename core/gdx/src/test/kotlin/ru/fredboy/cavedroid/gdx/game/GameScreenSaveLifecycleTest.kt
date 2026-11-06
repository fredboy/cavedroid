package ru.fredboy.cavedroid.gdx.game

import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.gdx.game.di.GameComponent

class GameScreenSaveLifecycleTest {
    private val component = mockk<GameComponent>(relaxed = true)
    private val screen = GameScreen(applicationContextRepository = mockk(), mobParamsRepository = mockk()).apply {
        GameScreen::class.java.getDeclaredField("gameComponent").apply { isAccessible = true }.set(this, component)
    }

    @Test
    fun `pause drains before suspending game controllers`() {
        screen.pause()
        verifyOrder {
            component.gameSaveHelper.drain()
            component.gameProc.onGamePaused()
        }
    }

    @Test
    fun `hide drains before hiding game controllers`() {
        screen.hide()
        verifyOrder {
            component.gameSaveHelper.drain()
            component.gameProc.hide()
        }
    }

    @Test
    fun `teardown drains and closes helper before disposing game controllers`() {
        screen.dispose()
        verifyOrder {
            component.gameSaveHelper.dispose()
            component.deathScreen.dispose()
            component.gameProc.dispose()
        }
    }
}
