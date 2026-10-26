package ru.fredboy.cavedroid.gameplay.physics.task

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.entity.mob.MobQueue
import ru.fredboy.cavedroid.entity.mob.abstraction.MobFactory
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig

class GameWorldMobSpawnControllerTaskTest {
    private val previousApp = Gdx.app
    private val world = mockk<GameWorld>(relaxed = true)
    private val mobs = mockk<MobController>(relaxed = true)
    private val factory = mockk<MobFactory>(relaxed = true)
    private val params = mockk<MobParams>(relaxed = true)
    private val repository = mockk<MobParamsRepository>()
    private val air = mockk<Block>(relaxed = true)
    private val ground = mockk<Block>(relaxed = true)
    private lateinit var task: GameWorldMobSpawnControllerTask
    private var floorY = 130
    private var airBrightness = 0f

    @BeforeEach
    fun setUp() {
        Gdx.app = mockk(relaxed = true)
        every { world.isInfinite } returns true
        every { world.height } returns 256
        every { world.generatorConfig } returns WorldGeneratorConfig.getDefault(seed = 42).copy(seaLevel = 128, lavaLevel = 131)
        every { world.isDayTime() } returns true
        every { world.getForeMap(any(), any()) } answers { if (secondArg<Int>() >= floorY) ground else air }
        every { world.getLightAt(any(), any()) } answers { if (secondArg<Int>() >= floorY) 0f else airBrightness }
        every { ground.params.hasCollision } returns true
        every { ground.params.key } returns "stone"
        every { ground.getRectangle(any(), any()) } answers { Rectangle(firstArg<Int>().toFloat(), secondArg<Int>().toFloat(), 1f, 1f) }
        every { params.key } returns "zombie"
        every { params.behaviorType } returns MobBehaviorType.AGGRESSIVE
        every { params.width } returns 0.625f
        every { params.height } returns 1.625f
        every { params.spawnWeight } returns 1f
        every { params.maxSpawnLight } returns 0.5f
        every { repository.getAllParams() } returns listOf(params)
        every { mobs.mobs } returns emptyList()
        task = GameWorldMobSpawnControllerTask(mobs, world, repository, factory, MobQueue())
    }

    @AfterEach
    fun tearDown() {
        Gdx.app = previousApp
    }

    @Test
    fun `lit cave prevents hostile spawning even though the solid floor is dark`() {
        airBrightness = 0.8f

        task.exec()

        verify(exactly = 0) { factory.create(any(), any(), any()) }
        verify(atLeast = 1) { world.getLightAt(any(), floorY - 1) }
    }

    @Test
    fun `dark cave still allows hostile spawning during the day`() {
        val spawnY = floorY - params.height / 2f

        task.exec()

        verify(atLeast = 1) { factory.create(any(), spawnY, "zombie") }
    }

    @Test
    fun `light at the configured limit still allows spawning`() {
        airBrightness = params.maxSpawnLight

        task.exec()

        verify(atLeast = 1) { factory.create(any(), any(), "zombie") }
    }

    @Test
    fun `lit surface prevents hostile spawning at night`() {
        floorY = 64
        airBrightness = 0.8f
        every { world.isDayTime() } returns false

        task.exec()

        verify(exactly = 0) { factory.create(any(), any(), any()) }
        verify(atLeast = 1) { world.getLightAt(any(), floorY - 1) }
    }
}
