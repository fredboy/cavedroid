package ru.fredboy.cavedroid.gameplay.controls.input.handler

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Timer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindow
import ru.fredboy.cavedroid.gameplay.controls.input.action.KeyboardInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.KeyboardInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.handler.keyboard.DropItemKeyboardInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse.CloseGameWindowMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse.HotbarMouseInputHandler

class CursorItemDropTest {
    private val item = InventoryItem(mockk<Item.Normal>(relaxed = true), 12)
    private val drops = mockk<DropController>(relaxed = true)

    @Test
    fun `Q drops one item at the world cursor without a throw impulse`() {
        val player = mockk<PlayerAdapter>(relaxed = true) {
            every { activeItem } returns item
            every { cursorX } returns 14.25f
            every { cursorY } returns 17.5f
        }
        val handler = DropItemKeyboardInputHandler(mockk(), player, drops)

        handler.handle(KeyboardInputAction(KeyboardInputActionKey.DropItem, true))

        verify(exactly = 1) { drops.addDrop(14.25f, 17.5f, match { it.item === item.item && it.amount == 1 }, null) }
        verify(exactly = 1) { player.decreaseCurrentItemCount(1) }
    }

    @Test
    fun `hotbar hold uses the cursor position at the time the hold completes`() {
        val player = mockk<Player>(relaxed = true) {
            every { inventory.items[0] } returns item
            every { cursorX } returns 14.25f
            every { cursorY } returns 17.5f
        }
        val mobs = mockk<MobController> { every { this@mockk.player } returns player }
        val inventory = player.inventory
        val textures = mockk<GetTextureRegionByNameUseCase>()
        every { textures["hotbar"] } returns mockk<TextureRegion> { every { regionWidth } returns 182 }
        val handler = HotbarMouseInputHandler(
            mockk { every { getCameraContext() } returns CameraContext(Rectangle(0f, 0f, 640f, 360f), Rectangle()) },
            mockk(),
            mobs,
            drops,
            textures,
        )
        val originalApp = Gdx.app
        Gdx.app = mockk<Application>(relaxed = true)
        mockkStatic(Timer::class)
        try {
            lateinit var hold: Timer.Task
            every { Timer.schedule(any<Timer.Task>(), any<Float>()) } answers {
                firstArg<Timer.Task>().also { hold = it }
            }
            handler.handle(MouseInputAction(230f, 0f, MouseInputActionKey.Screen(false, 0)))
            every { player.cursorX } returns 15.75f
            hold.run()

            verify(exactly = 1) { drops.addDrop(15.75f, 17.5f, match { it.item === item.item && it.amount == 1 }, null) }
            verify(exactly = 1) { inventory.decreaseItemAmount(0, 1) }
        } finally {
            handler.reset()
            unmockkStatic(Timer::class)
            Gdx.app = originalApp
        }
    }

    @Test
    fun `clicking outside the desktop inventory drops the held stack at the world cursor`() {
        val window = mockk<AbstractInventoryWindow>(relaxed = true) { every { selectedItem } returns item }
        val windows = mockk<GameWindowsManager>(relaxed = true) { every { currentWindow } returns window }
        val mobs = mockk<MobController>(relaxed = true) {
            every { player.cursorX } returns 14.25f
            every { player.cursorY } returns 17.5f
        }
        val handler = CloseGameWindowMouseInputHandler(mockk(), windows, mobs, drops, mockk())

        handler.handle(MouseInputAction(0f, 0f, MouseInputActionKey.Left(false)))

        verify(exactly = 1) { drops.addDrop(14.25f, 17.5f, item.item, 12, item.durability, null) }
        verify(exactly = 1) { window.selectedItem = null }
        verify(exactly = 1) { windows.closeWindow() }
    }
}
