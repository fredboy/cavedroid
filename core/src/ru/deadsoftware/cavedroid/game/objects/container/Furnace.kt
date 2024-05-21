package ru.deadsoftware.cavedroid.game.objects.container

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.TimeUtils
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.model.dto.SaveDataDto
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem.Companion.isNoneOrNull
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.misc.Saveable

class Furnace @JvmOverloads constructor(
    gameItemsHolder: GameItemsHolder,
    initialItems: List<InventoryItem>? = null
) : Container(SIZE, gameItemsHolder, initialItems), Saveable {

    var fuel: InventoryItem
        get() = items[FUEL_INDEX]
        set(value) {
            items[FUEL_INDEX] = value
        }

    var input: InventoryItem
        get() = items[INPUT_INDEX]
        set(value) {
            items[INPUT_INDEX] = value
        }

    var result: InventoryItem
        get() = items[RESULT_INDEX]
        set(value) {
            items[RESULT_INDEX] = value
        }

    val isActive: Boolean get() = currentFuel != null

    @Transient
    var currentFuel: Item? = null
        set(value) {
            currentFuelKey = value?.params?.key
            field = value
        }

    var currentFuelKey: String? = null

    private var startBurnTimeMs = 0L
    private var smeltStarTimeMs = 0L

    var burnProgress = 0f
        private set(value) {
            field = MathUtils.clamp(value, 0f, 1f)
        }
    var smeltProgress = 0f
        private set(value) {
            field = MathUtils.clamp(value, 0f, 1f)
        }

    fun init(gameItemsHolder: GameItemsHolder) {
        currentFuel = currentFuelKey?.let { gameItemsHolder.getItem(it) }
        items.forEach { it.init(gameItemsHolder) }
    }

    fun canSmelt(): Boolean {
        return (result.isNoneOrNull() || (result.item.params.key == input.item.params.smeltProductKey)) &&
                !input.isNoneOrNull() && input.item.params.smeltProductKey != null &&
                (!fuel.isNoneOrNull() || burnProgress > 0f)
    }

    private fun startBurning(gameItemsHolder: GameItemsHolder) {
        requireNotNull(fuel.item.params.burningTimeMs) { "Cant start burning without fuel" }
        currentFuel = fuel.item
        fuel.subtract()
        if (fuel.amount <= 0) {
            fuel = gameItemsHolder.fallbackItem.toInventoryItem()
        }
        startBurnTimeMs = TimeUtils.millis()
        burnProgress = 0f
    }

    override fun update(gameItemsHolder: GameItemsHolder) {
        if (currentFuel?.isNone() == true) {
            currentFuel = null
        }

        currentFuel?.let { curFuel ->
            val burningTimeMs = curFuel.params.burningTimeMs ?: run {
                Gdx.app.error(TAG, "Burning item has no burning time. Item : ${curFuel.params.key}")
                return
            }

            if (TimeUtils.timeSinceMillis(startBurnTimeMs).toDouble() / burningTimeMs >= 0.01) {
                burnProgress += 0.01f
                startBurnTimeMs = TimeUtils.millis()
            }

        }

        if (currentFuel?.isNone() == false && burnProgress >= 1f) {
            if (canSmelt()) {
                startBurning(gameItemsHolder)
            } else {
                currentFuel = null
                burnProgress = 0f
                smeltProgress = 0f
            }
        }

        if (!canSmelt()) {
            return
        }
        if (currentFuel == null && !fuel.isNoneOrNull()) {
            startBurning(gameItemsHolder)
            smeltStarTimeMs = startBurnTimeMs
            smeltProgress = 0f
        }

        if ((TimeUtils.timeSinceMillis(smeltStarTimeMs).toDouble() / SMELTING_TIME_MS) >= 0.01) {
            smeltProgress += 0.01f
            smeltStarTimeMs = TimeUtils.millis()
        }

        if (isActive && smeltProgress >= 1f) {
            val productKey = requireNotNull(input.item.params.smeltProductKey)
            val res = gameItemsHolder.getItem(productKey)
            if (result.isNoneOrNull()) {
                result = res.toInventoryItem()
            } else {
                result.add()
            }
            input.subtract()
            if (input.amount <= 0) {
                input = gameItemsHolder.fallbackItem.toInventoryItem()
            }
            smeltStarTimeMs = TimeUtils.millis()
            smeltProgress = 0f
        }
    }

    override fun getSaveData(): SaveDataDto.FurnaceSaveData {
        return SaveDataDto.FurnaceSaveData(
            version = SAVE_DATA_VERSION,
            size = size,
            currentFuelItemKey = currentFuelKey,
            items = items.map(InventoryItem::getSaveData),
            startBurnTimeMs = startBurnTimeMs,
            startSmeltTimeMs = smeltStarTimeMs,
            burnProgress = burnProgress,
            smeltProgress = smeltProgress,
        )
    }

    companion object {
        private const val SAVE_DATA_VERSION = 1
        private const val SIZE = 3
        private const val TAG = "Furnace"

        const val FUEL_INDEX = 0
        const val INPUT_INDEX = 1
        const val RESULT_INDEX = 2

        const val SMELTING_TIME_MS = 10000L

        fun fromSaveData(saveData: SaveDataDto.FurnaceSaveData, gameItemsHolder: GameItemsHolder): Furnace {
            saveData.verifyVersion(SAVE_DATA_VERSION)

            return Furnace(
                gameItemsHolder = gameItemsHolder,
                initialItems = saveData.items.map { item ->
                    InventoryItem.fromSaveData(item, gameItemsHolder)
                }
            ).apply {
                currentFuelKey = saveData.currentFuelItemKey
                startBurnTimeMs = saveData.startSmeltTimeMs
                smeltStarTimeMs = saveData.startSmeltTimeMs
                burnProgress = saveData.burnProgress
                smeltProgress = saveData.smeltProgress
            }
        }
    }

}