package ru.fredboy.cavedroid.gameplay.physics.task

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.entity.mob.MobQueue
import ru.fredboy.cavedroid.entity.mob.abstraction.MobFactory
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig

class CreeperSpawnTest {
    private val params = MobParams("Creeper", "creeper", 0.625f, 1.625f, 2.5f, MobBehaviorType.CREEPER, emptyList(), 20, emptyList(), 15f, 0, false, 20, 0.5f, 0.08f)
    private val world = mockk<GameWorld>(relaxed = true)
    private val mobs = mockk<MobController>(relaxed = true)
    private val factory = mockk<MobFactory>(relaxed = true)
    private val repository = mockk<MobParamsRepository> { every { getAllParams() } returns listOf(params) }
    private val queue = MobQueue()
    private val air = mockk<Block>(relaxed = true)
    private val ground = mockk<Block>(relaxed = true)
    private val previousApp = Gdx.app
    private lateinit var task: GameWorldMobSpawnControllerTask

    @BeforeEach
    fun setUp() {
        Gdx.app = mockk(relaxed = true)
        task = GameWorldMobSpawnControllerTask(mobs, world, repository, factory, queue)
    }

    @AfterEach
    fun tearDown() {
        Gdx.app = previousApp
    }

    init {
        every { world.isInfinite } returns true
        every { world.height } returns 256
        every { world.width } returns 256
        every { world.generatorConfig } returns WorldGeneratorConfig.getDefault(seed = 42)
        every { world.isDayTime() } returns false
        every { world.getForeMap(any(), any()) } answers { if (secondArg<Int>() == 64) ground else air }
        every { ground.params.hasCollision } returns true
        every { ground.params.key } returns "grass"
        every { ground.getRectangle(any(), any()) } answers { Rectangle(firstArg<Int>().toFloat(), secondArg<Int>().toFloat(), 1f, 1f) }
        every { mobs.mobs } returns emptyList()
        every { mobs.player.position } returns Vector2(0f, 63f)
        every { factory.create(any(), any(), "creeper") } answers {
            val mob = mockk<Mob>(relaxed = true) { every { this@mockk.params } returns this@CreeperSpawnTest.params }
            queue.offerMob(firstArg(), secondArg(), mob)
            mob
        }
    }

    @Test
    fun `natural spawns count queued creepers before their physics bodies are created`() {
        repeat(8) { task.onChunkBodiesReady(-3) }
        assertEquals(2, queue.queue.size)
    }

    @Test
    fun `natural spawns within sixteen blocks of the player are rejected`() {
        task.onChunkBodiesReady(0)
        verify(exactly = 0) { factory.create(any(), any(), any()) }
    }

    @Test
    fun `daytime surface spawns never select creepers`() {
        every { world.isDayTime() } returns true
        task.onChunkBodiesReady(-3)
        verify(exactly = 0) { factory.create(any(), any(), any()) }
    }
}
