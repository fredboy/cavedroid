package ru.fredboy.cavedroid.gameplay.controls.action.useblock.bed

import com.badlogic.gdx.math.Vector2
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld

class UseBedLeftActionTest {
    private val world = mockk<GameWorld>(relaxed = true)
    private val player = mockk<Player>(relaxed = true)
    private val mobs = mockk<MobController>(relaxed = true) {
        every { player } returns this@UseBedLeftActionTest.player
        every { playerCanSleep() } returns true
    }
    private val action = UseBedLeftAction(world, mobs, mockk(relaxed = true))

    init {
        every { player.position } returns Vector2(33.5f, 20f)
    }

    @Test
    fun `mounted player cannot sleep or queue a move to the bed`() {
        every { player.isMounted } returns true
        assertTrue(action.perform(mockk(), 36, 20))
        verify(exactly = 0) { world.skipNight() }
        verify(exactly = 0) { player.isInBed = true }
        verify(exactly = 0) { player.applyPendingTransform(any()) }
    }

    @Test
    fun `unmounted player can still sleep at night`() {
        assertTrue(action.perform(mockk(), 36, 20))
        verify { world.skipNight() }
        verify { player.isInBed = true }
        verify { player.applyPendingTransform(any()) }
    }
}
