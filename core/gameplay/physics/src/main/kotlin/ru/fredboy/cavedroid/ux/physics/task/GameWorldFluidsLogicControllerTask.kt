package ru.fredboy.cavedroid.gameplay.physics.task

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import java.util.*
import javax.inject.Inject
import kotlin.math.min
import kotlin.reflect.KClass

@GameScope
class GameWorldFluidsLogicControllerTask @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val itemsRepository: ItemsRepository,
) : BaseGameWorldControllerTask() {

    private var updateTick: Short = 0

    private val fluidStatesMap = mutableMapOf<KClass<out Block.Fluid>, List<Block.Fluid>>()

    private val updateQueue = PriorityQueue<UpdateCommand>(16) { c1, c2 ->
        c1.priority.compareTo(c2.priority)
    }

    init {
        val waters = itemsRepository.getBlocksByType(Block.Water::class.java)
            .sortedBy(Block.Water::state)
        val lavas = itemsRepository.getBlocksByType(Block.Lava::class.java)
            .sortedBy(Block.Lava::state)

        fluidStatesMap[Block.Water::class] = waters
        fluidStatesMap[Block.Lava::class] = lavas
    }

    private fun getNextStateBlock(fluid: Block.Fluid): Block.Fluid? {
        val stateList = fluidStatesMap[fluid::class] ?: return null
        val currentState = stateList.indexOf(fluid)
            .takeIf { it >= 0 } ?: return null

        var nextState = currentState + 1

        if (nextState == 1) {
            nextState++
        }

        if (nextState < stateList.size) {
            return stateList[nextState]
        }

        return null
    }

    private fun noFluidNearby(x: Int, y: Int): Boolean {
        val current = gameWorld.getForeMap(x, y)

        if (current !is Block.Fluid) {
            throw IllegalArgumentException("block at $x;$y is not a fluid")
        }

        val onTop = gameWorld.getForeMap(x, y - 1)
        val onLeft = gameWorld.getForeMap(x - 1, y)
        val onRight = gameWorld.getForeMap(x + 1, y)

        return !onTop.isFluid() &&
            (onLeft !is Block.Fluid || onLeft.state >= current.state) &&
            (onRight !is Block.Fluid || onRight.state >= current.state)
    }

    private fun drainFluid(x: Int, y: Int): Boolean {
        val fluid = (gameWorld.getForeMap(x, y) as? Block.Fluid)
            ?: return true

        if (fluid.state > 0) {
            if (noFluidNearby(x, y)) {
                val nexState = getNextStateBlock(fluid)
                if (nexState == null) {
                    updateQueue.offer(UpdateCommand(-1) { gameWorld.resetForeMap(x, y) })
                    return true
                }
                updateQueue.offer(UpdateCommand(nexState, x, y))
            }
        }

        return false
    }

    private fun fluidCanFlowThere(fluid: Block.Fluid, targetBlock: Block): Boolean {
        return targetBlock.isNone() ||
            (!targetBlock.params.hasCollision && !targetBlock.isFluid()) ||
            (fluid::class == targetBlock::class && fluid.state < (targetBlock as Block.Fluid).state)
    }

    private fun flowFluidTo(currentFluid: Block.Fluid, x: Int, y: Int, nextStateFluid: Block.Fluid) {
        val targetBlock = gameWorld.getForeMap(x, y)

        val command = when {
            fluidCanFlowThere(currentFluid, targetBlock) -> UpdateCommand(nextStateFluid, x, y)

            currentFluid.isWater() && targetBlock is Block.Lava && targetBlock.state > 0 ->
                UpdateCommand(100) { gameWorld.setForeMap(x, y, itemsRepository.getBlockByKey("cobblestone")) }

            currentFluid.isWater() && targetBlock.isLava() ->
                UpdateCommand(100) { gameWorld.setForeMap(x, y, itemsRepository.getBlockByKey("obsidian")) }

            currentFluid.isLava() && targetBlock.isWater() ->
                UpdateCommand(200) { gameWorld.setForeMap(x, y, itemsRepository.getBlockByKey("stone")) }

            else -> null
        }

        command?.let(updateQueue::offer)
    }

    private fun flowFluid(x: Int, y: Int) {
        val fluid = gameWorld.getForeMap(x, y) as Block.Fluid
        val stateList = fluidStatesMap[fluid::class] ?: return

        if (fluid.state < stateList.lastIndex && gameWorld.getForeMap(x, y + 1).params.hasCollision) {
            val nextState = getNextStateBlock(fluid) ?: return

            flowFluidTo(fluid, x - 1, y, nextState)
            flowFluidTo(fluid, x + 1, y, nextState)
        } else {
            flowFluidTo(fluid, x, y + 1, stateList[1])
        }
    }

    fun updateFluids(x: Int, y: Int) {
        val block = gameWorld.getForeMap(x, y)
        if (!block.isFluid() || (block.isLava() && updateTick % 2 == 0)) {
            return
        }

        if (drainFluid(x, y)) {
            return
        }

        flowFluid(x, y)
    }

    private fun fluidUpdater() {
        val midScreen = mobController.player.mapX

        for (y in gameWorld.height - 1 downTo 0) {
            for (x in 0..<min(gameWorld.width / 2, 32)) {
                updateFluids(midScreen + x, y)
                updateFluids(midScreen - x, y)
            }
        }

        while (!updateQueue.isEmpty()) {
            updateQueue.poll().exec()
        }
    }

    override fun exec() {
        if (updateTick < 0xFF) {
            updateTick++
        } else {
            updateTick = 1
        }

        fluidUpdater()
    }

    private inner class UpdateCommand(
        val priority: Int,
        val command: Runnable,
    ) {

        constructor(block: Block, x: Int, y: Int, priority: Int) :
            this(priority, Runnable { gameWorld.setForeMap(x, y, block) })

        constructor(fluid: Block.Fluid, x: Int, y: Int) :
            this(fluid, x, y, ((5 - fluid.state) + 1) * (if (fluid.isLava()) 2 else 1))

        fun exec() = command.run()
    }

    companion object {
        const val FLUID_UPDATE_INTERVAL_SEC = 0.25f
    }
}
