package ru.deadsoftware.cavedroid.game.objects.container

import com.badlogic.gdx.Gdx
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.deadsoftware.cavedroid.game.model.dto.SaveDataDto
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem.Companion.isNoneOrNull
import ru.deadsoftware.cavedroid.game.objects.drop.DropController
import ru.deadsoftware.cavedroid.misc.Saveable
import ru.deadsoftware.cavedroid.misc.utils.px
import java.io.Serializable
import javax.inject.Inject

@GameScope
class ContainerController @Inject constructor(
    _dropController: DropController,
    _gameItemsHolder: GameItemsHolder
) : Serializable, Saveable {

    @Suppress("UNNECESSARY_LATEINIT")
    @Transient
    private lateinit var dropController: DropController

    @Suppress("UNNECESSARY_LATEINIT")
    @Transient
    private lateinit var gameItemsHolder: GameItemsHolder

    private val containerMap = mutableMapOf<String, Container>()

    val size get() = containerMap.size

    init {
        dropController = _dropController
        gameItemsHolder = _gameItemsHolder
    }

    fun init(dropController: DropController, gameItemsHolder: GameItemsHolder) {
        this.dropController = dropController
        this.gameItemsHolder = gameItemsHolder
        containerMap.forEach { (_, container) -> container.initItems(gameItemsHolder) }
    }

    fun getContainer(x: Int, y: Int, z: Int): Container? {
        return containerMap["$x;$y;$z"]
    }

    fun addContainer(x: Int, y: Int, z: Int, clazz: Class<out Block.Container>) {
        val container = when (clazz) {
            Block.Furnace::class.java -> Furnace(gameItemsHolder)
            Block.Chest::class.java -> Chest(gameItemsHolder)
            else -> {
                Gdx.app.error(TAG, "Unknown container class $clazz")
                return
            }
        }
        containerMap["$x;$y;$z"] = container
    }

    @JvmOverloads
    fun destroyContainer(x: Int, y: Int, z: Int, dropItems: Boolean = true) {
        val container = containerMap.remove("$x;$y;$z") ?: return

        if (!dropItems) {
            return
        }

        val xPx = (x + .5f).px
        val yPx = (y + .5f).px

        container.items.forEach { item ->
            if (!item.isNoneOrNull()) {
                dropController.addDrop(xPx, yPx, item)
            }
        }
    }

    fun update() {
        containerMap.forEach { (_, container) ->
            container.update(gameItemsHolder)
        }
    }

    override fun getSaveData(): SaveDataDto.ContainerControllerSaveData {
        return SaveDataDto.ContainerControllerSaveData(
            version = SAVE_DATA_VERSION,
            containerMap = containerMap.mapValues { (_, container) -> container.getSaveData() },
        )
    }

    companion object {
        private const val SAVE_DATA_VERSION = 1
        private const val TAG = "ContainerController"

        fun fromSaveData(
            saveData: SaveDataDto.ContainerControllerSaveData,
            dropController: DropController,
            gameItemsHolder: GameItemsHolder
        ): ContainerController {
            saveData.verifyVersion(SAVE_DATA_VERSION)

            return ContainerController(
                dropController,
                gameItemsHolder
            ).apply {
                containerMap.putAll(
                    saveData.containerMap.mapValues { (_, containerSaveData) ->
                        Container.fromSaveData(containerSaveData, gameItemsHolder)
                    }
                )
            }
        }
    }

}