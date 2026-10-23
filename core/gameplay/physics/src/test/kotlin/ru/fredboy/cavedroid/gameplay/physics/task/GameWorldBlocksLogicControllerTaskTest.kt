package ru.fredboy.cavedroid.gameplay.physics.task

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.model.block.FenceBlocks
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.updateblock.IUpdateBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.updateblock.UpdateRequiresBlockAction

class GameWorldBlocksLogicControllerTaskTest {

    private val previousApp = Gdx.app
    private val world = mockk<GameWorld>(relaxed = true)
    private val action = mockk<IUpdateBlockAction>(relaxed = true)
    private val placed = slot<OnBlockPlacedListener>()
    private val air = Block.None(mockk<CommonBlockParams>(relaxed = true))
    private val fence = Block.Normal(
        mockk<CommonBlockParams>(relaxed = true).also {
            every { it.updateActionKey } returns FenceBlocks.UPDATE_ACTION_KEY
        },
    )
    private val actions = mapOf(
        FenceBlocks.UPDATE_ACTION_KEY to action,
        UpdateRequiresBlockAction.ACTION_KEY to mockk<IUpdateBlockAction>(relaxed = true),
    )

    init {
        Gdx.app = mockk<Application>(relaxed = true)
        every { world.height } returns 16
        every { world.width } returns 32
        every { world.isInfinite } returns true
        every { world.isChunkLoadedAt(any()) } returns true
        every { world.addBlockPlacedListener(capture(placed)) } answers { }
        every { world.getForeMap(any(), any()) } returns air
        every { world.getBackMap(any(), any()) } returns air
    }

    @AfterEach
    fun restoreApplication() {
        Gdx.app = previousApp
    }

    @Test
    fun `background placement queues fence updates with empty foreground`() {
        val task = GameWorldBlocksLogicControllerTask(world, actions)
        every { world.getBackMap(5, 5) } returns fence
        placed.captured.onBlockPlaced(fence, 5, 5, Layer.BACKGROUND)
        task.exec()
        verify(exactly = 1) { action.update(5, 5) }
        task.dispose()
    }

    @Test
    fun `negative chunk edge queues the fence in the adjacent chunk`() {
        val task = GameWorldBlocksLogicControllerTask(world, actions)
        every { world.getForeMap(0, 5) } returns fence
        placed.captured.onBlockPlaced(fence, -1, 5, Layer.FOREGROUND)
        repeat(2) { task.exec() }
        verify(exactly = 1) { action.update(0, 5) }
        task.dispose()
    }

    @Test
    fun `loading a neighbor rechecks an existing edge fence`() {
        val task = GameWorldBlocksLogicControllerTask(world, actions)
        every { world.getForeMap(-1, 5) } returns fence
        task.onChunkLoaded(0)
        repeat(3) { task.exec() }
        verify(exactly = 1) { action.update(-1, 5) }
        task.dispose()
    }

    @Test
    fun `looped seam updates canonical coordinates once`() {
        every { world.isInfinite } returns false
        val task = GameWorldBlocksLogicControllerTask(world, actions)
        every { world.getForeMap(31, 5) } returns fence
        placed.captured.onBlockPlaced(fence, 0, 5, Layer.FOREGROUND)
        repeat(2) { task.exec() }
        verify(exactly = 1) { action.update(31, 5) }
        task.dispose()
    }
}
