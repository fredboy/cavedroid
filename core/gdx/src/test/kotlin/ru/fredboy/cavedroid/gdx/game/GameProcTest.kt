package ru.fredboy.cavedroid.gdx.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.LifecycleListener
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.performance.NoOpPerformanceMonitor
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.GameInputProcessor
import ru.fredboy.cavedroid.gameplay.rendering.GameRenderer

class GameProcTest {
    @Test
    fun `unfinished terrain and lighting views do not pause movement or input`() {
        val previousApp = Gdx.app
        val listeners = mutableListOf<LifecycleListener>()
        Gdx.app = mockk(relaxed = true) {
            every { addLifecycleListener(any()) } answers { listeners.add(firstArg()) }
        }
        try {
            val world = mockk<GameWorld>(relaxed = true)
            val mobs = mockk<MobController>(relaxed = true)
            val renderer = mockk<GameRenderer>(relaxed = true)
            val input = mockk<GameInputProcessor>(relaxed = true)
            val proc = GameProc(
                NoOpPerformanceMonitor,
                mockk(relaxed = true),
                world,
                mobs,
                mockk(relaxed = true),
                renderer,
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                input,
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
            )
            try {
                clearMocks(world, mobs, renderer, input, answers = false)
                every { renderer.isReady } returns false
                repeat(3) { proc.update(1f / 60f) }
                every { renderer.isReady } returns true
                proc.update(1f / 60f)

                verify(exactly = 4) { world.update(1f / 60f) }
                verify(exactly = 4) { mobs.update(1f / 60f) }
                verify(exactly = 4) { input.update(1f / 60f) }
                verify(exactly = 4) { renderer.render(1f / 60f) }
                verify(exactly = 0) { world.resetPresentation() }
            } finally {
                proc.dispose()
            }
        } finally {
            listeners.forEach { it.dispose() }
            Gdx.app = previousApp
        }
    }
}
