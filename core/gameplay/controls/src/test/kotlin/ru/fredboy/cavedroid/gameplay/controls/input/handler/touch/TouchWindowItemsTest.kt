package ru.fredboy.cavedroid.gameplay.controls.input.handler.touch

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Timer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext
import ru.fredboy.cavedroid.domain.items.model.inventory.Inventory
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem.Companion.isNoneOrNull
import ru.fredboy.cavedroid.domain.items.model.item.CommonItemParams
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.TouchWindowLayout
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.CreativeInventoryTabsWindow
import ru.fredboy.cavedroid.game.window.inventory.CreativeInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.SurvivalInventoryWindow
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey

class TouchWindowItemsTest {
    private val originalApp = Gdx.app
    private val none = Item.None(mockk { every { maxStack } returns 64 })
    private val resource = Item.Normal(mockk<CommonItemParams> { every { maxStack } returns 64 }, mockk())
    private val items = mockk<ItemsRepository> {
        every { fallbackItem } returns none
        every { getCraftingResult(any()) } returns InventoryItem(none)
    }
    private val inventory = Inventory(36, none)
    private val window = SurvivalInventoryWindow(items)
    private val layout = TouchWindowLayout(640f, 360f, window)
    private val mobs = mockk<MobController> { every { player.inventory } returns inventory }
    private val windows = mockk<GameWindowsManager>(relaxed = true) { every { currentWindow } returns window }
    private val drops = mockk<DropQueue>(relaxed = true)
    private val player = mockk<PlayerAdapter>(relaxed = true) {
        every { direction } returns Direction.RIGHT
        every { x } returns 10f
        every { y } returns 20f
        every { height } returns 2f
    }
    private var scrollAmount = 0
    private val handler = TouchWindowInputHandler(
        mockk(),
        mockk { every { getCameraContext() } returns CameraContext(Rectangle(0f, 0f, 640f, 360f), Rectangle()) },
        windows, mobs, items, mockk(), player, drops, mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true),
    )
    private lateinit var hold: Timer.Task

    @BeforeEach
    fun setUp() {
        Gdx.app = mockk<Application>(relaxed = true)
        mockkStatic(Timer::class)
        every { Timer.schedule(any<Timer.Task>(), any<Float>()) } answers {
            firstArg<Timer.Task>().also { hold = it }
        }
        inventory.items[9] = InventoryItem(resource, 12)
        every { windows.creativeScrollAmount } answers { scrollAmount }
        every { windows.creativeScrollAmount = any() } answers { scrollAmount = firstArg() }
        every { items.getAllItems() } returns List(100) { resource }
    }

    @AfterEach
    fun tearDown() {
        handler.reset()
        unmockkStatic(Timer::class)
        Gdx.app = originalApp
    }

    private fun event(index: Int, key: MouseInputActionKey) {
        val rect = layout.slots.first { it.kind == TouchWindowLayout.Kind.INVENTORY && it.index == index }.bounds
        handler.handle(MouseInputAction(rect.x + rect.width / 2, rect.y + rect.height / 2, key))
    }

    @Test
    fun `drag moves a whole stack between the enlarged slots`() {
        event(9, MouseInputActionKey.Screen(false, 0))
        event(10, MouseInputActionKey.Dragged(0))
        event(10, MouseInputActionKey.Screen(true, 0))
        assertTrue(inventory.items[9].isNoneOrNull())
        assertEquals(12, inventory.items[10].amount)
        assertTrue(window.selectedItem.isNoneOrNull())
    }

    @Test
    fun `hold splits a stack and another finger places one item`() {
        event(9, MouseInputActionKey.Screen(false, 0))
        hold.run()
        assertEquals(6, inventory.items[9].amount)
        assertEquals(6, window.selectedItem?.amount)
        event(10, MouseInputActionKey.Screen(false, 1))
        event(10, MouseInputActionKey.Screen(true, 1))
        assertEquals(1, inventory.items[10].amount)
        assertEquals(5, window.selectedItem?.amount)
        assertEquals(6, inventory.items[9].amount)
    }

    @Test
    fun `releasing a stack into a gap keeps it available instead of dropping it`() {
        event(9, MouseInputActionKey.Screen(false, 0))
        handler.handle(MouseInputAction(288f, 180f, MouseInputActionKey.Dragged(0)))
        handler.handle(MouseInputAction(288f, 180f, MouseInputActionKey.Screen(true, 0)))
        assertEquals(12, window.selectedItem?.amount)
        assertTrue(inventory.items[9].isNoneOrNull())
    }

    @Test
    fun `cancelling a gesture does not pick up the pending stack`() {
        event(9, MouseInputActionKey.Screen(false, 0))
        handler.reset()
        assertEquals(12, inventory.items[9].amount)
        assertTrue(window.selectedItem.isNoneOrNull())
    }

    @Test
    fun `drop is explicit and creative inventory retains its trash action`() {
        val held = InventoryItem(resource, 12)
        window.selectedItem = held
        val rect = layout.drop
        fun tapDrop() {
            handler.handle(MouseInputAction(rect.x + rect.width / 2, rect.y + rect.height / 2, MouseInputActionKey.Screen(false, 0)))
            handler.handle(MouseInputAction(rect.x + rect.width / 2, rect.y + rect.height / 2, MouseInputActionKey.Screen(true, 0)))
        }
        tapDrop()
        verify(exactly = 1) { drops.offerItem(any(), any(), held, any()) }
        assertTrue(window.selectedItem.isNoneOrNull())
        val creative = CreativeInventoryTabsWindow(items).apply {
            selectedTab = CreativeInventoryTabsWindow.Tab.Inventory
            selectedItem = held
        }
        every { windows.currentWindow } returns creative
        tapDrop()
        verify(exactly = 1) { drops.offerItem(any(), any(), held, any()) }
        assertTrue(creative.selectedItem.isNoneOrNull())
    }

    private fun creative(tabbed: Boolean = true): Pair<AbstractInventoryWindow, TouchWindowLayout> {
        val creative = if (tabbed) {
            CreativeInventoryTabsWindow(items).apply { selectedTab = CreativeInventoryTabsWindow.Tab.Normal }
        } else {
            CreativeInventoryWindow(items)
        }
        every { windows.currentWindow } returns creative
        return creative to TouchWindowLayout(640f, 360f, creative)
    }

    private fun at(rect: Rectangle, key: MouseInputActionKey) {
        handler.handle(MouseInputAction(rect.x + rect.width / 2, rect.y + rect.height / 2, key))
    }

    @Test
    fun `holding a catalog item activates a full stack and the next move drags it into inventory`() {
        listOf(false, true).forEachIndexed { index, tabbed ->
            val (creative, geometry) = creative(tabbed)
            val source = geometry.slots.first { it.kind == TouchWindowLayout.Kind.CATALOG }.bounds
            at(source, MouseInputActionKey.Screen(false, 0))
            assertTrue(creative.selectedItem.isNoneOrNull())
            hold.run()
            assertEquals(64, creative.selectedItem?.amount)
            event(10 + index, MouseInputActionKey.Dragged(0))
            event(10 + index, MouseInputActionKey.Screen(true, 0))
            assertEquals(64, inventory.items[10 + index].amount)
            assertEquals(12, inventory.items[9].amount)
            assertTrue(creative.selectedItem.isNoneOrNull())
            assertEquals(0, scrollAmount)
        }
    }

    @Test
    fun `catalog swipes scroll before activation and cancel the hold`() {
        val (creative, geometry) = creative()
        val source = geometry.slots.first { it.kind == TouchWindowLayout.Kind.CATALOG }.bounds
        at(source, MouseInputActionKey.Screen(false, 0))
        handler.handle(MouseInputAction(source.x + 5, source.y - source.height * 2, MouseInputActionKey.Dragged(0)))
        hold.run()
        assertTrue(creative.selectedItem.isNoneOrNull())
        assertTrue(scrollAmount > 0)
    }

    @Test
    fun `neither catalog swipes nor the scrollbar scroll while an item is selected`() {
        val (creative, geometry) = creative()
        val source = geometry.slots.first { it.kind == TouchWindowLayout.Kind.CATALOG }.bounds
        at(source, MouseInputActionKey.Screen(false, 0))
        hold.run()
        handler.handle(MouseInputAction(source.x + 5, source.y - source.height * 2, MouseInputActionKey.Dragged(0)))
        at(geometry.scroll, MouseInputActionKey.Screen(false, 1))
        handler.handle(MouseInputAction(geometry.scroll.x + 2, geometry.scroll.y + geometry.scroll.height - 2, MouseInputActionKey.Dragged(1)))
        at(geometry.scroll, MouseInputActionKey.Screen(true, 1))
        assertEquals(0, scrollAmount)
        assertEquals(64, creative.selectedItem?.amount)
    }

    @Test
    fun `quick catalog taps and cancelled holds do not create items`() {
        val (creative, geometry) = creative()
        val source = geometry.slots.first { it.kind == TouchWindowLayout.Kind.CATALOG }.bounds
        at(source, MouseInputActionKey.Screen(false, 0))
        at(source, MouseInputActionKey.Screen(true, 0))
        hold.run()
        assertTrue(creative.selectedItem.isNoneOrNull())
        at(source, MouseInputActionKey.Screen(false, 0))
        handler.reset()
        hold.run()
        assertTrue(creative.selectedItem.isNoneOrNull())
        assertEquals(12, inventory.items[9].amount)
    }

    @Test
    fun `dragging onto Drop throws the stack away from the player in the facing direction`() {
        for (direction in Direction.entries) {
            every { player.direction } returns direction
            inventory.items[9] = InventoryItem(resource, 12)
            event(9, MouseInputActionKey.Screen(false, 0))
            at(layout.drop, MouseInputActionKey.Dragged(0))
            at(layout.drop, MouseInputActionKey.Screen(true, 0))
            verify { drops.offerItem(10f + 2.5f * direction.basis, 19f, match { it.amount == 12 }, Vector2(50f * direction.basis, -50f)) }
            assertTrue(window.selectedItem.isNoneOrNull())
            assertTrue(inventory.items[9].isNoneOrNull())
        }
    }

    @Test
    fun `small finger movements do not cancel creative hold activation`() {
        val (creative, geometry) = creative()
        val source = geometry.slots.first { it.kind == TouchWindowLayout.Kind.CATALOG }.bounds
        at(source, MouseInputActionKey.Screen(false, 0))
        handler.handle(MouseInputAction(source.x + source.width / 2 + 1, source.y + source.height / 2 + 1, MouseInputActionKey.Dragged(0)))
        hold.run()
        assertEquals(64, creative.selectedItem?.amount)
        assertEquals(0, scrollAmount)
    }

    @Test
    fun `changing creative categories cancels the pending item activation`() {
        val (creative, geometry) = creative()
        val source = geometry.slots.first { it.kind == TouchWindowLayout.Kind.CATALOG }.bounds
        at(source, MouseInputActionKey.Screen(false, 0))
        (creative as CreativeInventoryTabsWindow).selectedTab = CreativeInventoryTabsWindow.Tab.Inventory
        hold.run()
        assertTrue(creative.selectedItem.isNoneOrNull())
        handler.handle(MouseInputAction(source.x, source.y, MouseInputActionKey.Dragged(0)))
        assertTrue(creative.selectedItem.isNoneOrNull())
    }
}
