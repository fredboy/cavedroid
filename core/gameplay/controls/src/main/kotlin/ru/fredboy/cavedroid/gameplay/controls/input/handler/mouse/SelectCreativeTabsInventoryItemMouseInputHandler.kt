package ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.api.InventoryHintEvents
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.TooltipManager
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem.Companion.isNoneOrNull
import ru.fredboy.cavedroid.domain.items.model.inventory.asSafeInventoryList
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.stats.repository.StatsRepository
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.model.WearingArmor.Companion.BOOTS_INDEX
import ru.fredboy.cavedroid.entity.mob.model.WearingArmor.Companion.CHESTPLATE_INDEX
import ru.fredboy.cavedroid.entity.mob.model.WearingArmor.Companion.HELMET_INDEX
import ru.fredboy.cavedroid.entity.mob.model.WearingArmor.Companion.LEGGINGS_INDEX
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs.CreativeTabs
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.CreativeInventoryTabsWindow
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindMouseInputHandler
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class SelectCreativeTabsInventoryItemMouseInputHandler @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameContextRepository: GameContextRepository,
    private val gameWindowsManager: GameWindowsManager,
    private val mobController: MobController,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val itemsRepository: ItemsRepository,
    private val tooltipManager: TooltipManager,
    inventoryHintEvents: InventoryHintEvents,
    statsRepository: StatsRepository,
    playerAdapter: PlayerAdapter,
    dropQueue: DropQueue,
) : AbstractInventoryItemsMouseInputHandler(
    applicationContextRepository = applicationContextRepository,
    gameContextRepository = gameContextRepository,
    itemsRepository = itemsRepository,
    gameWindowsManager = gameWindowsManager,
    windowType = GameWindowType.CREATIVE_INVENTORY_TABS,
    inventoryHintEvents = inventoryHintEvents,
    statsRepository = statsRepository,
    playerAdapter = playerAdapter,
    dropQueue = dropQueue,
) {

    private val inventoryTabWindowTexture get() = requireNotNull(textureRegions["creative_inventory_tab"])
    private val itemsTabWindowTexture get() = requireNotNull(textureRegions["creative_items_tab"])
    private val selectedTabTexture get() = requireNotNull(textureRegions["creative_tab_selected"])
    override val windowTexture
        get() = if ((gameWindowsManager.currentWindow as CreativeInventoryTabsWindow).selectedTab.isInventory) {
            inventoryTabWindowTexture
        } else {
            itemsTabWindowTexture
        }

    override fun getWindowRect(viewport: Rectangle): Rectangle {
        return Rectangle(
            0f,
            0f,
            windowTexture.regionWidth.toFloat(),
            windowTexture.regionHeight.toFloat() + selectedTabTexture.regionHeight,
        ).apply {
            setCenter(viewport.getCenter(Vector2()))
        }
    }

    override fun checkConditions(action: MouseInputAction): Boolean {
        return super.checkConditions(action) &&
            !(
                applicationContextRepository.isTouch() &&
                    (gameWindowsManager.currentWindow as? CreativeInventoryTabsWindow)
                        ?.selectedTab?.isInventory == false &&
                    (
                        action.actionKey is MouseInputActionKey.Dragged ||
                            gameWindowsManager.isDragging ||
                            !action.actionKey.touchUp
                        )
                )
    }

    private fun handleInsideCreativeGrid(action: MouseInputAction, xOnGrid: Int, yOnGrid: Int) {
        val window = gameWindowsManager.currentWindow as CreativeInventoryTabsWindow

        val itemIndex = xOnGrid + yOnGrid * CreativeTabs.itemsInRow +
            gameWindowsManager.creativeScrollAmount * CreativeTabs.itemsInRow

        val items = itemsRepository.getAllItems().asSequence()
            .filter { window.selectedTab.isRelevantItem(it) }
            .map { it.toInventoryItem(durability = it.durabilityOrNull() ?: 1) }
            .toMutableList()
            .asSafeInventoryList(itemsRepository)

        if (applicationContextRepository.isTouch()) {
            val item = items[itemIndex].item
            mobController.player.inventory.addItem(item)
            tooltipManager.showHotbarTooltip(item.params.name)
        } else {
            handleInsidePlaceableCell(
                action = action,
                items = items,
                window = window,
                index = itemIndex,
            )
        }
    }

    private fun handleInsideInventoryGrid(action: MouseInputAction, xOnGrid: Int, yOnGrid: Int) {
        val window = gameWindowsManager.currentWindow as CreativeInventoryTabsWindow

        var itemIndex = xOnGrid + yOnGrid * CreativeTabs.itemsInRow
        itemIndex += CreativeTabs.hotbarCells

        handleInsidePlaceableCell(action, mobController.player.inventory.items, window, itemIndex)
    }

    private fun handleInsideHotbar(action: MouseInputAction, index: Int) {
        val window = gameWindowsManager.currentWindow as CreativeInventoryTabsWindow

        handleInsidePlaceableCell(action, mobController.player.inventory.items, window, index)
    }

    private fun handleInsideArmorCell(action: MouseInputAction, armorCell: Int) {
        val window = gameWindowsManager.currentWindow as CreativeInventoryTabsWindow
        val selectedItem = window.selectedItem

        if (selectedItem != null &&
            !selectedItem.item.isNone() &&
            !mobController.player.wearingArmor.getCellType(armorCell).isInstance(selectedItem.item) &&
            selectedItem.amount != 0
        ) {
            return
        }

        handleInsidePlaceableCell(action, mobController.player.wearingArmor.items, window, armorCell)
    }

    override fun handle(action: MouseInputAction) {
        val window = gameWindowsManager.currentWindow as CreativeInventoryTabsWindow

        val windowX = gameContextRepository.getCameraContext().viewport.width / 2 - windowTexture.regionWidth / 2
        val windowY = gameContextRepository.getCameraContext().viewport.height / 2 -
            (windowTexture.regionHeight + selectedTabTexture.regionHeight) / 2

        val xOnWindow = action.screenX - windowX
        val yOnWindow = action.screenY - windowY

        val xOnInvGrid = (xOnWindow - CreativeTabs.itemsGridMarginLeft) / CreativeTabs.itemsGridColWidth
        val yOnInvGrid = (yOnWindow - CreativeTabs.itemsGridMarginTop) / CreativeTabs.itemsGridRowHeight
        val xOnItemsGrid = (xOnWindow - CreativeTabs.itemsGridMarginLeft) / CreativeTabs.itemsGridColWidth
        val yOnItemsGrid = (yOnWindow - CreativeTabs.itemsGridMarginTopItemsTab) / CreativeTabs.itemsGridRowHeight

        val armorCell = when {
            xOnWindow >= CreativeTabs.headX &&
                xOnWindow <= CreativeTabs.headX + CreativeTabs.itemsGridColWidth &&
                yOnWindow >= CreativeTabs.headY &&
                yOnWindow <= CreativeTabs.headY + CreativeTabs.itemsGridRowHeight -> HELMET_INDEX

            xOnWindow >= CreativeTabs.bodyX &&
                xOnWindow <= CreativeTabs.bodyX + CreativeTabs.itemsGridColWidth &&
                yOnWindow >= CreativeTabs.bodyY &&
                yOnWindow <= CreativeTabs.bodyY + CreativeTabs.itemsGridRowHeight -> CHESTPLATE_INDEX

            xOnWindow >= CreativeTabs.legsX &&
                xOnWindow <= CreativeTabs.legsX + CreativeTabs.itemsGridColWidth &&
                yOnWindow >= CreativeTabs.legsY &&
                yOnWindow <= CreativeTabs.legsY + CreativeTabs.itemsGridRowHeight -> LEGGINGS_INDEX

            xOnWindow >= CreativeTabs.feetX &&
                xOnWindow <= CreativeTabs.feetX + CreativeTabs.itemsGridColWidth &&
                yOnWindow >= CreativeTabs.feetY &&
                yOnWindow <= CreativeTabs.feetY + CreativeTabs.itemsGridRowHeight -> BOOTS_INDEX

            else -> null
        }?.takeIf { window.selectedTab.isInventory }

        val isInsideInventoryGrid = window.selectedTab.isInventory &&
            xOnInvGrid >= 0 &&
            xOnInvGrid < CreativeTabs.itemsInRow &&
            yOnInvGrid >= 0 &&
            yOnInvGrid < CreativeTabs.itemsInInvCol

        val isInsideHotbar =
            xOnInvGrid in 0f..<CreativeTabs.hotbarCells.toFloat() &&
                yOnWindow >= windowTexture.regionHeight - CreativeTabs.hotbarOffsetFromBottom &&
                yOnWindow <= windowTexture.regionHeight

        val isInsideTrash =
            xOnInvGrid.toInt() == CreativeTabs.hotbarCells &&
                yOnWindow >= windowTexture.regionHeight - CreativeTabs.hotbarOffsetFromBottom &&
                yOnWindow <= windowTexture.regionHeight

        val isInsideItemsGrid = !window.selectedTab.isInventory &&
            xOnItemsGrid >= 0 &&
            xOnItemsGrid < CreativeTabs.itemsInRow &&
            yOnItemsGrid >= 0 &&
            yOnItemsGrid < CreativeTabs.itemsInCol

        val clickedTab =
            (xOnWindow / (windowTexture.regionWidth / CreativeInventoryTabsWindow.Tab.entries.size)).toInt()
                .takeIf { it in CreativeInventoryTabsWindow.Tab.entries.indices }
                ?.takeIf { yOnWindow > windowTexture.regionHeight }
                ?.let { CreativeInventoryTabsWindow.Tab.entries[it] }

        if (isInsideInventoryGrid) {
            handleInsideInventoryGrid(action, xOnInvGrid.toInt(), yOnInvGrid.toInt())
        } else if (armorCell != null) {
            handleInsideArmorCell(action, armorCell)
        } else if (isInsideItemsGrid) {
            handleInsideCreativeGrid(action, xOnItemsGrid.toInt(), yOnItemsGrid.toInt())
        } else if (isInsideHotbar) {
            handleInsideHotbar(action, xOnInvGrid.toInt())
        } else if (clickedTab != null && window.selectedItem.isNoneOrNull()) {
            window.selectedTab = clickedTab
            gameWindowsManager.creativeScrollAmount = 0
        } else if (isInsideTrash) {
            handleInsidePlaceableCell(action, mutableListOf(itemsRepository.fallbackItem.toInventoryItem()), window, 0)
        } else {
            handleOutsideAnyCell(action, window)
        }
    }

    private fun Item.durabilityOrNull() = (this as? Item.Durable)?.durability
}
