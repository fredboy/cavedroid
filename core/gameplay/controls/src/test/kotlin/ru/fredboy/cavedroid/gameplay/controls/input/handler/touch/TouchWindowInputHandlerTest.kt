package ru.fredboy.cavedroid.gameplay.controls.input.handler.touch

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.Rectangle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.domain.assets.repository.UiSoundAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.TouchWindowLayout
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindow
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey

class TouchWindowInputHandlerTest {
    private val held = mockk<InventoryItem>(relaxed = true)
    private val window = mockk<AbstractInventoryWindow>(relaxed = true) {
        every { type } returns GameWindowType.CHEST
        every { selectedItem } returns held
    }
    private val windows = mockk<GameWindowsManager>(relaxed = true) { every { currentWindow } returns window }
    private val drops = mockk<DropQueue>(relaxed = true)
    private val mobs = mockk<MobController>(relaxed = true)
    private val click = mockk<Sound>()
    private val soundPlayer = mockk<SoundPlayer>(relaxed = true)
    private val uiSounds = mockk<UiSoundAssetsRepository> { every { getClickSound() } returns click }
    private val handler = TouchWindowInputHandler(
        mockk<ApplicationContextRepository> { every { isTouch() } returns true },
        mockk<GameContextRepository> { every { getCameraContext() } returns CameraContext(Rectangle(0f, 0f, 640f, 360f), Rectangle()) },
        windows, mobs, mockk(relaxed = true), mockk(), mockk(relaxed = true), drops, mockk(relaxed = true), mockk(relaxed = true), soundPlayer, uiSounds,
    )

    @Test
    fun `background taps and drag releases preserve the held item and window`() {
        handler.handle(MouseInputAction(288f, 180f, MouseInputActionKey.Screen(false, 0)))
        handler.handle(MouseInputAction(288f, 180f, MouseInputActionKey.Dragged(0)))
        handler.handle(MouseInputAction(288f, 180f, MouseInputActionKey.Screen(true, 0)))
        assertSame(held, window.selectedItem)
        verify(exactly = 0) { windows.closeWindow() }
        verify(exactly = 0) { window.selectedItem = any() }
        verify(exactly = 0) { drops.offerItem(any(), any(), any()) }
    }

    @Test
    fun `releasing an item over close or drop without pressing there does nothing`() {
        val layout = TouchWindowLayout(640f, 360f, window)
        listOf(layout.close, layout.drop).forEach { rect ->
            handler.handle(MouseInputAction(rect.x + rect.width / 2, rect.y + rect.height / 2, MouseInputActionKey.Screen(true, 0)))
        }
        verify(exactly = 0) { windows.closeWindow() }
        verify(exactly = 0) { window.selectedItem = any() }
        verify(exactly = 0) { drops.offerItem(any(), any(), any()) }
    }

    @Test
    fun `explicit close requires the same pointer to press and release`() {
        val rect = TouchWindowLayout(640f, 360f, window).close
        fun action(up: Boolean, pointer: Int) = MouseInputAction(rect.x + rect.width / 2, rect.y + rect.height / 2, MouseInputActionKey.Screen(up, pointer))
        every { mobs.player.inventory.getAvailableSlotForItem(any()) } returns -1
        handler.handle(action(false, 0))
        verify(exactly = 0) { soundPlayer.playUiSound(any()) }
        handler.handle(action(true, 1))
        verify(exactly = 0) { windows.closeWindow() }
        handler.handle(action(true, 0))
        verify(exactly = 1) { windows.closeWindow() }
        verify(exactly = 1) { soundPlayer.playUiSound(click) }
    }

    @Test
    fun `letterbox taps cannot dismiss the window or drop its held item`() {
        for ((x, y) in listOf(320f to 10f, 600f to 350f)) {
            handler.handle(MouseInputAction(x, y, MouseInputActionKey.Screen(false, 0)))
            handler.handle(MouseInputAction(x, y, MouseInputActionKey.Screen(true, 0)))
        }
        verify(exactly = 0) { windows.closeWindow() }
        verify(exactly = 0) { window.selectedItem = any() }
        verify(exactly = 0) { drops.offerItem(any(), any(), any()) }
    }
}
