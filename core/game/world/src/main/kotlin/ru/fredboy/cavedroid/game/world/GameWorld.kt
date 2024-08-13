package ru.fredboy.cavedroid.game.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.px
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.container.model.Chest
import ru.fredboy.cavedroid.game.controller.container.model.Container
import ru.fredboy.cavedroid.game.controller.container.model.Furnace
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.model.Player
import ru.fredboy.cavedroid.game.world.generator.GameWorldGenerator
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig
import javax.inject.Inject

@GameScope
class GameWorld @Inject constructor(
    private val itemsRepository: ItemsRepository,
    private val containerController: ContainerController,
    private val mobController: MobController,
    private val dropController: DropController,
    initialForeMap: Array<Array<Block>>?,
    initialBackMap: Array<Array<Block>>?,
) {
    val foreMap: Array<Array<Block>>
    val backMap: Array<Array<Block>>

    val width: Int
    val height: Int

    val generatorConfig = WorldGeneratorConfig.getDefault()

    init {
        width = generatorConfig.width
        height = generatorConfig.height

        val (generatedFore, generatedBack) = GameWorldGenerator(generatorConfig, itemsRepository).generate()
        foreMap = generatedFore
        backMap = generatedBack
    }

    private fun transformX(x: Int): Int {
        var transformed = x % width
        if (transformed < 0) {
            transformed = width + x
        }
        return transformed
    }

    private fun getMap(x: Int, y: Int, layer: Int): Block {
        val fallback = itemsRepository.fallbackBlock

        if (y !in 0 ..< height) {
            return fallback
        }

        val transformedX = transformX(x)

        if (transformedX !in 0 ..< width) {
            return fallback
        }

        return when (layer) {
            FOREGROUND_Z -> foreMap[transformedX][y]
            BACKGROUND_Z -> backMap[transformedX][y]
            else -> {
                Gdx.app.error(TAG, "Unexpected value for layer in getMap ($layer). Returning fallback")
                fallback
            }
        }
    }

    private fun setMap(x: Int, y: Int, layer: Int, value: Block) {
        if (y !in 0 ..< height) {
            return
        }

        val transformedX = transformX(x)

        if (transformedX !in 0 ..< width) {
            return
        }

        containerController.destroyContainer(x, y, layer)

        if (value.isContainer()) {
            when {
                value.isChest() -> Chest(itemsRepository.fallbackItem)
                value.isFurnace() -> Furnace(itemsRepository.fallbackItem)
                else -> {
                    Gdx.app.error(TAG, "Unknow container type: ${value::class.simpleName}")
                    null
                }
            }?.let { container ->
                containerController.addContainer(x, y, layer, container)
            }
        }

        when (layer) {
            FOREGROUND_Z -> foreMap[transformedX][y] = value
            BACKGROUND_Z -> backMap[transformedX][y] = value
            else -> {
                Gdx.app.error(TAG, "Unexpected value for layer in setMap ($layer). Returning fallback")
            }
        }
    }

    private fun isSameSlab(slab1: Block, slab2: Block): Boolean {
        return slab1 is Block.Slab && slab2 is Block.Slab &&
                (slab1.params.key == slab2.otherPartBlockKey || slab1.otherPartBlockKey == slab2.params.key)
    }

    fun hasForeAt(x: Int, y: Int): Boolean {
        return !getMap(x, y, FOREGROUND_Z).isNone()
    }

    fun hasBackAt(x: Int, y: Int): Boolean {
        return !getMap(x, y, BACKGROUND_Z).isNone()
    }

    fun getForeMap(x: Int, y: Int): Block {
        return getMap(x, y, FOREGROUND_Z)
    }

    fun setForeMap(x: Int, y: Int, block: Block) {
        setMap(x, y, FOREGROUND_Z, block)
    }

    fun resetForeMap(x: Int, y: Int) {
        setForeMap(x, y, itemsRepository.fallbackBlock)
    }

    fun getBackMap(x: Int, y: Int): Block {
        return getMap(x, y, BACKGROUND_Z)
    }

    fun setBackMap(x: Int, y: Int, block: Block) {
        setMap(x, y, BACKGROUND_Z, block)
    }

    fun canPlaceToForeground(x: Int, y: Int, value: Block): Boolean {
        return !hasForeAt(x, y) || value.isNone() || !getForeMap(x, y).params.hasCollision
    }

    fun placeToForeground(x: Int, y: Int, value: Block): Boolean {
        return if (canPlaceToForeground(x, y, value)) {
            setForeMap(x, y, value)
            true
        } else if (value is Block.Slab && isSameSlab(value, getForeMap(x, y))) {
            setForeMap(x, y, itemsRepository.getBlockByKey(value.otherPartBlockKey))
            true
        } else {
            false
        }
    }

    fun placeToBackground(x: Int, y: Int, value: Block): Boolean {
        return if (value.isNone() || getBackMap(x, y).isNone() && value.params.hasCollision &&
            (!value.params.isTransparent || value.params.key == "glass" || value.isChest() || value.isSlab())) {
            setBackMap(x, y, value)
            true
        } else {
            false
        }
    }

    fun checkPlayerCursorBounds() {
        with(mobController.player) {
            if (gameMode == 0) {
                val minCursorX = mapX - SURVIVAL_CURSOR_RANGE
                val maxCursorX = mapX + SURVIVAL_CURSOR_RANGE
                val minCursorY = middleMapY - SURVIVAL_CURSOR_RANGE
                val maxCursorY = middleMapY + SURVIVAL_CURSOR_RANGE

                cursorX = MathUtils.clamp(cursorX, minCursorX, maxCursorX)
                cursorY = MathUtils.clamp(cursorY, minCursorY, maxCursorY)
            }

            cursorY = MathUtils.clamp(cursorY, 0, this@GameWorld.height)
        }
    }

    fun playerDurateTool() {
        TODO()
    }

    private fun shouldDrop(block: Block): Boolean {
        TODO()
    }

    private fun spawnInitialMobs() {
        TODO()
    }

    fun destroyForeMap(x: Int, y: Int) {
        val block = getForeMap(x, y)

        if (block.isContainer()) {
            containerController.destroyContainer(x, y, FOREGROUND_Z)
        }

        block.params.dropInfo?.takeIf { shouldDrop(block) }?.let { dropInfo ->
            dropController.addDrop(
                x = transformX(x).px,
                y = y.px,
                item = itemsRepository.getItemByKey(dropInfo.itemKey),
                count = dropInfo.count,
            )
        }
        playerDurateTool()
        placeToForeground(x, y, itemsRepository.fallbackBlock)
    }

    fun destroyBackMap(x: Int, y: Int) {
        val block = getBackMap(x, y)

        if (block.isContainer()) {
            containerController.destroyContainer(x, y, BACKGROUND_Z)
        }

        block.params.dropInfo?.takeIf { shouldDrop(block) }?.let { dropInfo ->
            dropController.addDrop(
                x = transformX(x).px,
                y = y.px,
                item = itemsRepository.getItemByKey(dropInfo.itemKey),
                count = dropInfo.count,
            )
        }
        playerDurateTool()
        placeToBackground(x, y, itemsRepository.fallbackBlock)
    }

    private fun getContainerAt(x: Int, y: Int, layer: Int): Container? {
        return containerController.getContainer(x, y, layer)
    }

    fun getForegroundContainer(x: Int, y: Int): Container? {
        return getContainerAt(x, y, FOREGROUND_Z)
    }

    fun getBackgroundContainer(x: Int, y: Int): Container? {
        return getContainerAt(x, y, BACKGROUND_Z)
    }

    fun getForegroundFurnace(x: Int, y: Int): Furnace? {
        return getForegroundContainer(x, y) as? Furnace
    }

    fun getBackgroundFurnace(x: Int, y: Int): Furnace? {
        return getBackgroundContainer(x, y) as? Furnace
    }


    companion object {
        private const val TAG = "GameWorld"

        private const val FOREGROUND_Z = 0
        private const val BACKGROUND_Z = 1

        private const val SURVIVAL_CURSOR_RANGE = 4
    }
}