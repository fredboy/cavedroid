package ru.fredboy.cavedroid.gameplay.physics.task

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.game.controller.fire.FireController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld

class GameWorldFireLogicControllerTaskTest {
    private val previousApp = Gdx.app
    private val app = mockk<Application>(relaxed = true)
    private val world = mockk<GameWorld>(relaxed = true)
    private val fires = mockk<FireController>(relaxed = true)
    private val mobs = mockk<MobController>(relaxed = true)
    private val fire = FireController.FireInstance(0, 0, Layer.FOREGROUND, age = 7.5f)
    private val air = block(false)
    private val wood = block(true)
    private val task: GameWorldFireLogicControllerTask

    init {
        Gdx.app = app
        every { world.width } returns 16
        every { world.height } returns 1
        every { world.getForeMap(any(), any()) } returns air
        every { world.getBackMap(any(), any()) } returns air
        every { world.getForeMap(0, 0) } returns wood
        every { fires.snapshot() } returns listOf(fire)
        every { fires.getFireAt(0, 0, Layer.FOREGROUND) } returns fire
        task = GameWorldFireLogicControllerTask(world, fires, mobs)
    }

    @AfterEach
    fun restoreApplication() {
        Gdx.app = previousApp
    }

    @Test
    fun `expiry completes before tick returns with no deferred mutation`() {
        task.exec()
        verify(exactly = 1) { fires.removeFire(0, 0, Layer.FOREGROUND) }
        verify(exactly = 1) { world.destroyForeMap(0, 0, false, false) }
        verify(exactly = 0) { app.postRunnable(any()) }
    }

    @Test
    fun `age is committed before chunk eviction can snapshot it`() {
        fire.age = 2f
        task.exec()
        assertEquals(2.5f, fire.age)
        verify(exactly = 0) { app.postRunnable(any()) }
    }

    @Test
    fun `replaced support is not destroyed by an expired fire`() {
        every { world.getForeMap(0, 0) } returns air
        task.exec()
        verify(exactly = 1) { fires.removeFire(0, 0, Layer.FOREGROUND) }
        verify(exactly = 0) { world.destroyForeMap(any(), any(), any(), any()) }
    }

    @Test
    fun `shutdown prevents a scheduled tick from modifying the world`() {
        task.shutdownBlocking()
        task.run()
        verify(exactly = 0) { fires.snapshot() }
        verify(exactly = 0) { app.postRunnable(any()) }
    }

    private fun block(combustible: Boolean): Block {
        val params = mockk<CommonBlockParams>(relaxed = true)
        every { params.combustible } returns combustible
        return mockk<Block>(relaxed = true).also {
            every { it.params } returns params
        }
    }
}
