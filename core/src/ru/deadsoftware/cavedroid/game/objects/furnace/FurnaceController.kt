package ru.deadsoftware.cavedroid.game.objects.furnace

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.objects.drop.DropController
import ru.deadsoftware.cavedroid.misc.utils.px
import java.io.Serializable
import javax.inject.Inject

@GameScope
class FurnaceController @Inject constructor() : Serializable {

    private val furnaceMap = mutableMapOf<String, Furnace>()

    fun init(gameItemsHolder: GameItemsHolder) {
        furnaceMap.forEach { _, fur -> fur.init(gameItemsHolder) }
    }

    fun getFurnace(x: Int, y: Int, z: Int): Furnace? {
        return furnaceMap["$x;$y;$z"]
    }

    fun addFurnace(x: Int, y: Int, z: Int) {
        furnaceMap["$x;$y;$z"] = Furnace()
    }

    fun destroyFurnace(x: Int, y: Int, z: Int, dropController: DropController) {
        val furnace = furnaceMap.remove("$x;$y;$z") ?: return
        val xPx = (x + .5f).px
        val yPx = (y + .5f).px

        furnace.input?.let { dropController.addDrop(xPx, yPx, it) }
        furnace.fuel?.let { dropController.addDrop(xPx, yPx, it) }
        furnace.result?.let { dropController.addDrop(xPx, yPx, it) }
    }

    fun update(gameItemsHolder: GameItemsHolder) {
        furnaceMap.forEach { _, furnace ->
            furnace.update(gameItemsHolder)
        }
    }

}