package ru.fredboy.cavedroid.gameplay.physics.task

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.model.block.StemBlocks
import ru.fredboy.cavedroid.domain.save.model.GrowBlockEntry
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.IGrowBlockAction

class GameWorldGrowBlocksControllerTaskTest {

    private val previousApp = Gdx.app
    private val world = mockk<GameWorld>(relaxed = true)
    private val placed = slot<OnBlockPlacedListener>()
    private val action = mockk<IGrowBlockAction> {
        every { nextDelayTicks() } returns 2L
        every { grow(any(), any()) } returns false
    }
    private val key = "pumpkin_stem_stage7"
    private val stem = Block.Normal(
        mockk<CommonBlockParams>(relaxed = true) {
            every { key } returns this@GameWorldGrowBlocksControllerTaskTest.key
            every { growActionKey } returns StemBlocks.GROW_ACTION_KEY
        },
    )

    init {
        Gdx.app = mockk<Application>(relaxed = true)
        every { world.getForeMap(5, 10) } returns stem
        every { world.isInfinite } returns false
        every { world.addBlockPlacedListener(capture(placed)) } answers { }
    }

    @AfterEach
    fun restoreApplication() {
        Gdx.app = previousApp
    }

    @Test
    fun `shared action schedules mature stems repeatedly`() {
        val task = task()
        placed.captured.onBlockPlaced(stem, 5, 10, Layer.FOREGROUND)
        repeat(4) { task.exec() }

        verify(exactly = 2) { action.grow(5, 10) }
        assertEquals(listOf(GrowBlockEntry(5, 10, key, 2L)), task.snapshot())
        task.dispose()
    }

    @Test
    fun `saved stem timer resumes using the shared action`() {
        val task = task(listOf(GrowBlockEntry(5, 10, key, 1L)))
        task.exec()

        verify(exactly = 1) { action.grow(5, 10) }
        assertEquals(2L, task.snapshot().single().remainingTicks)
        task.dispose()
    }

    @Test
    fun `stale timers do not grow replacement blocks`() {
        val task = task()
        placed.captured.onBlockPlaced(stem, 5, 10, Layer.FOREGROUND)
        every { world.getForeMap(5, 10) } returns Block.None(mockk(relaxed = true))
        repeat(2) { task.exec() }

        verify(exactly = 0) { action.grow(any(), any()) }
        assertTrue(task.snapshot().isEmpty())
        task.dispose()
    }

    @Test
    fun `background stems do not enter the growth queue`() {
        val task = task()
        placed.captured.onBlockPlaced(stem, 5, 10, Layer.BACKGROUND)

        assertTrue(task.snapshot().isEmpty())
        task.dispose()
    }

    private fun task(entries: List<GrowBlockEntry> = emptyList()): GameWorldGrowBlocksControllerTask = GameWorldGrowBlocksControllerTask(
        world,
        mapOf(StemBlocks.GROW_ACTION_KEY to action),
        mockk(relaxed = true),
        mockk(relaxed = true),
        mockk(relaxed = true),
        initialEntries = entries,
    )
}
