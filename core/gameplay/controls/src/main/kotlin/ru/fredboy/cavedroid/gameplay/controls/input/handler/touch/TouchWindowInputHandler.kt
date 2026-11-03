package ru.fredboy.cavedroid.gameplay.controls.input.handler.touch

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Timer
import ru.fredboy.cavedroid.common.api.InventoryHintEvents
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.repository.UiSoundAssetsRepository
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem.Companion.isNoneOrNull
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.model.item.isNone
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.stats.repository.StatsRepository
import ru.fredboy.cavedroid.entity.container.model.Furnace
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.TouchWindowLayout
import ru.fredboy.cavedroid.game.window.TouchWindowLayout.Kind
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindowWithCraftGrid
import ru.fredboy.cavedroid.game.window.inventory.ChestInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.CreativeInventoryTabsWindow
import ru.fredboy.cavedroid.game.window.inventory.FurnaceInventoryWindow
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse.AbstractInventoryItemsMouseInputHandler
import javax.inject.Inject
import kotlin.math.abs

/** Owns all pointer events while a touch window is open, including its inert background. */
@GameScope
@BindMouseInputHandler
class TouchWindowInputHandler @Inject constructor(
    private val application: ApplicationContextRepository,
    private val context: GameContextRepository,
    private val windows: GameWindowsManager,
    private val mobs: MobController,
    private val items: ItemsRepository,
    private val textures: GetTextureRegionByNameUseCase,
    private val player: PlayerAdapter,
    private val drops: DropQueue,
    hints: InventoryHintEvents,
    stats: StatsRepository,
    private val soundPlayer: SoundPlayer,
    private val uiSounds: UiSoundAssetsRepository,
) : AbstractInventoryItemsMouseInputHandler(
    application, context, items, windows, GameWindowType.NONE, hints, stats, player, drops,
) {
    override val windowTexture get() = requireNotNull(textures["touch_survival"])
    override fun getWindowRect(viewport: Rectangle) = viewport

    private data class Gesture(
        val control: String?,
        val x: Float,
        var y: Float,
        var scrolled: Boolean = false,
        var dragged: Boolean = false,
        var hold: Timer.Task? = null,
    )
    private val gestures = mutableMapOf<Int, Gesture>()

    override fun checkConditions(action: MouseInputAction) = application.isTouch() && windows.currentWindow != null

    override fun reset() {
        super.reset()
        gestures.values.forEach { it.hold?.cancel() }
        gestures.clear()
        windows.isDragging = false
    }

    override fun handle(action: MouseInputAction) {
        val window = windows.currentWindow ?: return
        val viewport = context.getCameraContext().viewport
        val layout = TouchWindowLayout(viewport.width, viewport.height, window)
        val key = action.actionKey
        val pointer = when (key) {
            is MouseInputActionKey.Screen -> key.pointer
            is MouseInputActionKey.Dragged -> key.pointer
            else -> return
        }
        val slot = layout.slotAt(action.screenX, action.screenY)
        val control = when {
            !layout.bounds.contains(action.screenX, action.screenY) -> null
            layout.close.contains(action.screenX, action.screenY) -> "close"
            layout.drop.contains(action.screenX, action.screenY) -> "drop"
            layout.scroll.width > 0 && layout.scroll.contains(action.screenX, action.screenY) -> "scroll"
            window is AbstractInventoryWindowWithCraftGrid && layout.recipeToggle.contains(action.screenX, action.screenY) -> "recipes"
            layout.previous.contains(action.screenX, action.screenY) -> "previous"
            layout.next.contains(action.screenX, action.screenY) -> "next"
            slot?.kind == Kind.CATALOG -> "catalog"
            else -> layout.tabs.indexOfFirst { it.contains(action.screenX, action.screenY) }
                .takeIf { it >= 0 }?.let { "tab:$it" }
        }
        if (key is MouseInputActionKey.Screen && !key.touchUp) {
            gestures.remove(pointer)?.hold?.cancel()
            gestures[pointer] = Gesture(control, action.screenX, action.screenY)
            if (control == "catalog" && window.selectedItem.isNoneOrNull()) {
                val catalogItem = catalog(window).getOrNull(requireNotNull(slot).index + windows.creativeScrollAmount * layout.catalogColumns)
                if (catalogItem != null && !catalogItem.isNone()) {
                    val gesture = requireNotNull(gestures[pointer])
                    val tabAtPress = (window as? CreativeInventoryTabsWindow)?.selectedTab
                    gesture.hold = object : Timer.Task() {
                        override fun run() {
                            if (gestures[pointer] !== gesture || windows.currentWindow !== window || gesture.scrolled || !window.selectedItem.isNoneOrNull()) return
                            if (window is CreativeInventoryTabsWindow && window.selectedTab != tabAtPress) return
                            window.selectedItem = catalogItem.toInventoryItem(catalogItem.params.maxStack, (catalogItem as? Item.Durable)?.durability ?: 1)
                            window.selectItemPointer = pointer
                            gesture.hold = null
                        }
                    }
                    Timer.schedule(gesture.hold, CATALOG_HOLD_SECONDS)
                }
            }
        }
        val gesture = gestures[pointer]
        if (gesture?.control == "catalog" && layout.catalogColumns == 0) {
            gestures.remove(pointer)?.hold?.cancel()
            return
        }
        if (key is MouseInputActionKey.Dragged) gesture?.dragged = true
        if (gesture?.control == "scroll") {
            if (window.selectedItem.isNoneOrNull()) {
                val thumbHeight = TouchWindowLayout.MIN_BUTTON_SIZE * layout.scale
                val fraction = ((action.screenY - layout.scroll.y - thumbHeight / 2) / (layout.scroll.height - thumbHeight).coerceAtLeast(1f)).coerceIn(0f, 1f)
                windows.creativeScrollAmount = (fraction * layout.maxCatalogScroll(catalog(window).size)).toInt()
            }
            if (key is MouseInputActionKey.Screen && key.touchUp) gestures.remove(pointer)
            return
        }
        val ownsItem = !window.selectedItem.isNoneOrNull() && window.selectItemPointer == pointer
        if (gesture?.control == "catalog" && !ownsItem) {
            if (key is MouseInputActionKey.Dragged) {
                val slop = 8f * layout.scale
                if (!gesture.scrolled && abs(gesture.y - action.screenY) < slop && abs(gesture.x - action.screenX) < slop) return
                gesture.hold?.cancel()
                gesture.hold = null
                gesture.scrolled = true
                if (window.selectedItem.isNoneOrNull()) {
                    val rowHeight = layout.slots.first { it.kind == Kind.CATALOG }.bounds.height
                    val rows = ((gesture.y - action.screenY) / rowHeight).toInt()
                    if (rows != 0) {
                        windows.creativeScrollAmount = (windows.creativeScrollAmount + rows).coerceIn(0, layout.maxCatalogScroll(catalog(window).size))
                        gesture.y -= rows * rowHeight
                    }
                }
            } else if (key is MouseInputActionKey.Screen && key.touchUp) {
                gestures.remove(pointer)?.hold?.cancel()
            }
            return
        }
        if (slot?.kind == Kind.CATALOG) {
            if (key is MouseInputActionKey.Dragged || (key is MouseInputActionKey.Screen && key.touchUp)) cancelHold()
            if (key is MouseInputActionKey.Screen && key.touchUp) gestures.remove(pointer)?.hold?.cancel()
            return
        }
        if (key is MouseInputActionKey.Screen && key.touchUp) {
            gestures.remove(pointer)?.hold?.cancel()
            if (control != null && (gesture?.control == control || (control == "drop" && gesture?.dragged == true && ownsItem))) {
                cancelHold()
                if (!control.startsWith("tab:") || window.selectedItem.isNoneOrNull()) playClick()
                when (control) {
                    "close" -> {
                        // Return the held stack when possible; closing only drops any remainder.
                        window.selectedItem?.takeUnless { it.isNoneOrNull() }?.let { held ->
                            while (mobs.player.inventory.getAvailableSlotForItem(held.item) >= 0) {
                                if (mobs.player.inventory.pickUpItem(held)) {
                                    window.selectedItem = null
                                    break
                                }
                            }
                        }
                        windows.closeWindow()
                    }
                    "drop" -> {
                        val trash = window is CreativeInventoryTabsWindow && window.selectedTab.isInventory
                        if (!trash) {
                            window.selectedItem?.takeUnless { it.isNoneOrNull() }?.let {
                                val direction = player.direction.basis
                                drops.offerItem(player.x + DROP_DISTANCE * direction, player.y - player.height / 2, it, Vector2(50f * direction, -50f))
                            }
                        }
                        window.selectedItem = null
                    }
                    "recipes" -> (window as AbstractInventoryWindowWithCraftGrid).let {
                        it.recipeBookActive = !it.recipeBookActive
                        it.selectedRecipe = -1
                    }
                    "previous", "next" -> (window as? AbstractInventoryWindowWithCraftGrid)?.let {
                        val pages = (it.getAvailableCraftingRecipes(items).count() + TouchWindowLayout.RECIPES_PER_PAGE - 1) / TouchWindowLayout.RECIPES_PER_PAGE
                        it.recipeBookPage = (it.recipeBookPage + (if (control == "next") 1 else -1) + pages.coerceAtLeast(1)) % pages.coerceAtLeast(1)
                        it.selectedRecipe = -1
                    }
                    else -> if (control.startsWith("tab:") && window.selectedItem.isNoneOrNull()) {
                        (window as CreativeInventoryTabsWindow).selectedTab = CreativeInventoryTabsWindow.Tab.entries[control.substringAfter(':').toInt()]
                        windows.creativeScrollAmount = 0
                    }
                }
                return
            }
        }
        // A release on a gap cancels the hold timer but keeps a picked-up stack in hand.
        if (slot == null || control != null) {
            if (key is MouseInputActionKey.Dragged || key.touchUp) cancelHold()
            return
        }
        when (slot.kind) {
            Kind.INVENTORY -> handleInsidePlaceableCell(action, mobs.player.inventory.items, window, slot.index)
            Kind.CHEST -> handleInsidePlaceableCell(action, (window as ChestInventoryWindow).items, window, slot.index)
            Kind.ARMOR -> {
                val held = window.selectedItem
                if (held.isNoneOrNull() || mobs.player.wearingArmor.getCellType(slot.index).isInstance(held.item)) {
                    handleInsidePlaceableCell(action, mobs.player.wearingArmor.items, window, slot.index)
                }
            }
            Kind.CRAFT -> (window as AbstractInventoryWindowWithCraftGrid).let {
                handleInsidePlaceableCell(action, it.craftingItems, it, slot.index)
                updateCraftResult(it)
            }
            Kind.INPUT, Kind.FUEL -> (window as FurnaceInventoryWindow).let {
                if (slot.kind == Kind.INPUT || window.selectedItem.isNoneOrNull() || window.selectedItem?.item?.params?.burningTimeMs != null) {
                    handleInsidePlaceableCell(action, it.furnace.items, it, if (slot.kind == Kind.INPUT) Furnace.INPUT_INDEX else Furnace.FUEL_INDEX)
                }
            }
            Kind.RESULT -> {
                if (window is AbstractInventoryWindowWithCraftGrid) {
                    if (key is MouseInputActionKey.Screen && key.touchUp) return
                    window.selectedRecipe = -1
                    handleInsideCraftResultCell(action, window.craftResultList, window, 0)
                    updateCraftResult(window)
                } else {
                    handleInsideCraftResultCell(action, (window as FurnaceInventoryWindow).furnace.items, window, Furnace.RESULT_INDEX)
                }
            }
            Kind.RECIPE -> if (key is MouseInputActionKey.Screen && key.touchUp) {
                cancelHold()
                val craft = window as AbstractInventoryWindowWithCraftGrid
                if (slot.index >= craft.getVisibleCraftingRecipes(items, TouchWindowLayout.RECIPES_PER_PAGE).size) return
                playClick()
                if (craft.selectedRecipe == slot.index) {
                    craft.tryFillPhantomRecipe(player, drops, mobs.player.inventory, items, TouchWindowLayout.RECIPES_PER_PAGE)
                } else {
                    craft.clearCrafting(player, drops, items)
                }
                craft.selectedRecipe = slot.index
                updateCraftResult(craft)
            }
            Kind.CATALOG -> Unit
        }
    }

    private fun playClick() = soundPlayer.playUiSound(uiSounds.getClickSound())

    private fun catalog(window: AbstractInventoryWindow): List<Item> {
        val creative = window as? CreativeInventoryTabsWindow
        return creative?.getCatalogItems() ?: items.getAllItems().toList()
    }

    companion object {
        private const val CATALOG_HOLD_SECONDS = .35f
        private const val DROP_DISTANCE = 2.5f
    }
}
