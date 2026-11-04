package ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Timer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey

class HotbarMouseInputHandlerTest {
    private val originalApp = Gdx.app
    private val joystick = Joystick(4f).apply { activate(10f, 100f, 0) }
    private val context = mockk<GameContextRepository> {
        every { getJoystick() } returns joystick
        every { getCameraContext() } returns CameraContext(Rectangle(0f, 0f, 320f, 240f), Rectangle())
    }
    private val textures = mockk<GetTextureRegionByNameUseCase>()
    private val player = mockk<Player>(relaxed = true)
    private val mobs = mockk<MobController> { every { player } returns this@HotbarMouseInputHandlerTest.player }
    private val drops = mockk<DropController>(relaxed = true)
    private val windows = mockk<GameWindowsManager> { every { currentWindowType } returns GameWindowType.NONE }
    private val handler = HotbarMouseInputHandler(context, windows, mobs, drops, textures)
    private val scheduled = mutableListOf<Timer.Task>()

    @BeforeEach
    fun setUp() {
        every { textures["hotbar"] } returns mockk<TextureRegion> {
            every { regionWidth } returns 180
            every { regionHeight } returns 20
        }
        Gdx.app = mockk(relaxed = true)
        mockkStatic(Timer::class)
        every { Timer.schedule(any<Timer.Task>(), any<Float>()) } answers {
            firstArg<Timer.Task>().also { scheduled.add(it) }
        }
    }

    @AfterEach
    fun tearDown() {
        handler.reset()
        unmockkStatic(Timer::class)
        Gdx.app = originalApp
    }

    @Test
    fun `joystick movement and release cannot cancel a hotbar tap`() {
        down()
        val drag = MouseInputAction(30f, 100f, MouseInputActionKey.Dragged(0))
        val release = MouseInputAction(30f, 100f, MouseInputActionKey.Screen(true, 0))
        assertFalse(handler.checkConditions(drag))
        assertFalse(handler.checkConditions(release))
        // Ownership is also enforced when the handler is called directly.
        handler.handle(drag)
        handler.handle(release)
        up()
        verify(exactly = 1) { player.activeSlot = 4 }
    }

    @Test
    fun `another hotbar finger cannot replace the active press`() {
        down()
        val otherDown = MouseInputAction(200f, 10f, MouseInputActionKey.Screen(false, 2))
        val otherUp = otherDown.copy(actionKey = MouseInputActionKey.Screen(true, 2))
        assertFalse(handler.checkConditions(otherDown))
        assertFalse(handler.checkConditions(otherUp))
        handler.handle(otherDown)
        handler.handle(otherUp)
        up()
        verify(exactly = 1) { Timer.schedule(any<Timer.Task>(), any<Float>()) }
        verify(exactly = 1) { player.activeSlot = 4 }
    }

    @Test
    fun `joystick finger cannot begin a hotbar press`() {
        assertFalse(handler.checkConditions(MouseInputAction(160f, 10f, MouseInputActionKey.Screen(false, 0))))
    }

    @Test
    fun `dragging the hotbar finger cancels selection and permits a new tap`() {
        down()
        val drag = MouseInputAction(180f, 10f, MouseInputActionKey.Dragged(1))
        assertTrue(handler.checkConditions(drag))
        handler.handle(drag)
        assertFalse(handler.checkConditions(MouseInputAction(180f, 10f, MouseInputActionKey.Screen(true, 1))))
        handler.handle(MouseInputAction(180f, 10f, MouseInputActionKey.Screen(true, 1)))
        verify(exactly = 0) { player.activeSlot = any() }
        down()
        up()
        verify(exactly = 1) { player.activeSlot = 4 }
    }

    @Test
    fun `long press drops once without selecting on release and permits a new tap`() {
        val item = InventoryItem(mockk<Item.Normal>(relaxed = true), 12)
        every { player.inventory.items[4] } returns item
        val inventory = player.inventory
        down()
        scheduled.single().run()
        val otherRelease = MouseInputAction(30f, 100f, MouseInputActionKey.Screen(true, 0))
        assertFalse(handler.checkConditions(otherRelease))
        handler.handle(otherRelease)
        up()
        verify(exactly = 1) { drops.addDrop(any(), any(), match { it.item === item.item && it.amount == 1 }, null) }
        verify(exactly = 1) { inventory.decreaseItemAmount(4, 1) }
        verify(exactly = 0) { player.activeSlot = any() }
        down()
        up()
        verify(exactly = 1) { player.activeSlot = 4 }
    }

    @Test
    fun `reset forgets the pressed finger`() {
        down()
        handler.reset()
        assertFalse(handler.checkConditions(MouseInputAction(160f, 10f, MouseInputActionKey.Screen(true, 1))))
        down(pointer = 2)
        up(pointer = 2)
        verify(exactly = 1) { player.activeSlot = 4 }
    }

    @Test
    fun `release outside the hotbar does not select an invalid slot`() {
        down()
        val release = MouseInputAction(310f, 100f, MouseInputActionKey.Screen(true, 1))
        assertTrue(handler.checkConditions(release))
        handler.handle(release)
        verify(exactly = 0) { player.activeSlot = any() }
    }

    @Test
    fun `desktop click still selects and desktop drag cancels selection`() {
        val down = MouseInputAction(160f, 10f, MouseInputActionKey.Left(false))
        val up = down.copy(actionKey = MouseInputActionKey.Left(true))
        assertTrue(handler.checkConditions(down))
        handler.handle(down)
        assertTrue(handler.checkConditions(up))
        handler.handle(up)
        verify(exactly = 1) { player.activeSlot = 4 }
        handler.handle(down)
        val drag = MouseInputAction(180f, 10f, MouseInputActionKey.Dragged(0))
        assertTrue(handler.checkConditions(drag))
        handler.handle(drag)
        assertFalse(handler.checkConditions(up))
        handler.handle(up)
        verify(exactly = 1) { player.activeSlot = 4 }
    }

    @Test
    fun `scroll still wraps hotbar selection`() {
        var activeSlot = Player.HOTBAR_SIZE - 1
        every { player.activeSlot } answers { activeSlot }
        every { player.activeSlot = any() } answers { activeSlot = firstArg() }
        val scroll = MouseInputAction(0f, 0f, MouseInputActionKey.Scroll(0f, 1f))
        assertTrue(handler.checkConditions(scroll))
        handler.handle(scroll)
        verify { player.activeSlot = 0 }
    }

    private fun down(pointer: Int = 1) {
        val action = MouseInputAction(160f, 10f, MouseInputActionKey.Screen(false, pointer))
        assertTrue(handler.checkConditions(action))
        handler.handle(action)
    }

    private fun up(pointer: Int = 1) {
        val action = MouseInputAction(160f, 10f, MouseInputActionKey.Screen(true, pointer))
        assertTrue(handler.checkConditions(action))
        handler.handle(action)
    }
}
