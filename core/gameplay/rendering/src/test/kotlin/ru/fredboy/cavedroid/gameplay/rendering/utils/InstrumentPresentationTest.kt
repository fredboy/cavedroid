package ru.fredboy.cavedroid.gameplay.rendering.utils

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.world.GameWorld

class InstrumentPresentationTest {
    private val clock = Item.Instrument(mockk(), Item.Instrument.Kind.CLOCK, List(64) { mockk<Sprite>() })
    private val compass = Item.Instrument(mockk(), Item.Instrument.Kind.COMPASS, List(32) { mockk<Sprite>() })
    private val items = mockk<ItemsRepository> { every { getAllItems() } returns listOf(clock, compass) }
    private val world = mockk<GameWorld> {
        every { currentGameTime } returns 300f
        every { worldSpawn } returns Vector2(2f, 40f)
        every { isInfinite } returns false
        every { width } returns 100
    }
    private val player = mockk<PlayerAdapter> {
        every { x } returns 98f
        every { y } returns 40f
    }

    @Test
    fun `catalog sprites follow world time and shortest route to saved spawn`() {
        val presentation = InstrumentPresentation(items, world, player)
        presentation.update(0f)
        assertEquals(0, clock.frame)
        assertEquals(24, compass.frame)
        assertSame(clock.stateSprites[0], clock.sprite)
        assertSame(compass.stateSprites[24], compass.sprite)

        every { world.currentGameTime } returns 900f
        every { world.isInfinite } returns true
        presentation.update(0f)
        assertEquals(32, clock.frame)
        assertEquals(8, compass.frame)
    }

    @Test
    fun `new session resets shared catalog frames and compass interpolation`() {
        clock.frame = 17
        compass.frame = 17
        InstrumentPresentation(items, world, player).update(0.016f)
        assertEquals(0, clock.frame)
        assertEquals(24, compass.frame)
    }
}
