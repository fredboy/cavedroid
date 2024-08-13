package ru.fredboy.cavedroid.game.controller.container.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.TimeUtils
import ru.fredboy.cavedroid.domain.items.model.item.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.InventoryItem.Companion.isNoneOrNull
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase

class Furnace(
    fallbackItem: Item,
    initialItems: List<InventoryItem>? = null
) : Container(
    size = SIZE,
    fallbackItem = fallbackItem,
    initialItems = initialItems,
) {

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

    var currentFuel: Item? = null
        set(value) {
            currentFuelKey = value?.params?.key
            field = value
        }

    var currentFuelKey: String? = null

    var startBurnTimeMs = 0L
    var smeltStarTimeMs = 0L

    var burnProgress = 0f
        set(value) {
            field = MathUtils.clamp(value, 0f, 1f)
        }
    var smeltProgress = 0f
        set(value) {
            field = MathUtils.clamp(value, 0f, 1f)
        }


    fun canSmelt(): Boolean {
        return (result.isNoneOrNull() || (result.item.params.key == input.item.params.smeltProductKey)) &&
                !input.isNoneOrNull() && input.item.params.smeltProductKey != null &&
                (!fuel.isNoneOrNull() || burnProgress > 0f)
    }

    private fun startBurning() {
        requireNotNull(fuel.item.params.burningTimeMs) { "Cant start burning without fuel" }
        currentFuel = fuel.item
        fuel.subtract()
        if (fuel.amount <= 0) {
            fuel = fallbackItem.toInventoryItem()
        }
        startBurnTimeMs = TimeUtils.millis()
        burnProgress = 0f
    }

    override fun update(itemByKey: GetItemByKeyUseCase) {
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
                startBurning()
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
            startBurning()
            smeltStarTimeMs = startBurnTimeMs
            smeltProgress = 0f
        }

        if ((TimeUtils.timeSinceMillis(smeltStarTimeMs).toDouble() / SMELTING_TIME_MS) >= 0.01) {
            smeltProgress += 0.01f
            smeltStarTimeMs = TimeUtils.millis()
        }

        if (isActive && smeltProgress >= 1f) {
            val productKey = requireNotNull(input.item.params.smeltProductKey)
            val res = itemByKey[productKey]
            if (result.isNoneOrNull()) {
                result = res.toInventoryItem()
            } else {
                result.add()
            }
            input.subtract()
            if (input.amount <= 0) {
                input = fallbackItem.toInventoryItem()
            }
            smeltStarTimeMs = TimeUtils.millis()
            smeltProgress = 0f
        }
    }

    companion object {
        private const val SIZE = 3
        private const val TAG = "Furnace"

        const val FUEL_INDEX = 0
        const val INPUT_INDEX = 1
        const val RESULT_INDEX = 2

        const val SMELTING_TIME_MS = 10000L
    }

}