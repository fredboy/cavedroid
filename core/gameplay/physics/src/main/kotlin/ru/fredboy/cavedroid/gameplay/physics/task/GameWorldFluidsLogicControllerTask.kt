package ru.fredboy.cavedroid.gameplay.physics.task

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.performance.NoOpPerformanceMonitor
import ru.fredboy.cavedroid.common.performance.PerformanceMetric
import ru.fredboy.cavedroid.common.performance.PerformanceMonitor
import ru.fredboy.cavedroid.common.utils.PriorityMap
import ru.fredboy.cavedroid.common.utils.PriorityMapValue
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject
import kotlin.math.min
import kotlin.reflect.KClass

@GameScope
class GameWorldFluidsLogicControllerTask @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val itemsRepository: ItemsRepository,
    performanceMonitor: PerformanceMonitor = NoOpPerformanceMonitor,
) : BaseGameWorldControllerTask(performanceMonitor, PerformanceMetric.TASK_FLUIDS) {

    private var updateTick: Short = 0

    private val fluidStatesMap = mutableMapOf<KClass<out Block.Fluid>, List<Block.Fluid>>()

    private val updateMap = PriorityMap<Pair<Int, Int>, UpdateCommand>()

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

        return onTop::class != current::class &&
            (onLeft !is Block.Fluid || onLeft::class != current::class || onLeft.state >= current.state) &&
            (onRight !is Block.Fluid || onRight::class != current::class || onRight.state >= current.state)
    }

    private fun drainFluid(x: Int, y: Int): Boolean {
        val fluid = (gameWorld.getForeMap(x, y) as? Block.Fluid)
            ?: return true

        if (fluid.state > 0) {
            if (noFluidNearby(x, y)) {
                val nextState = getNextStateBlock(fluid)
                if (nextState == null) {
                    updateMap[x to y] = UpdateCommand(Int.MAX_VALUE, x, y) { gameWorld.resetForeMap(x, y) }
                    return true
                }
                updateMap[x to y] = UpdateCommand(nextState, x, y)
            }
        }

        return false
    }

    private fun fluidCanFlowThere(fluid: Block.Fluid, targetBlock: Block): Boolean {
        return targetBlock.isNone() ||
            (targetBlock.params.replaceable && !targetBlock.isFluid()) ||
            (fluid::class == targetBlock::class && fluid.state < (targetBlock as Block.Fluid).state)
    }

    private fun flowFluidTo(currentFluid: Block.Fluid, x: Int, y: Int, nextStateFluid: Block.Fluid): Boolean {
        val targetBlock = gameWorld.getForeMap(x, y)

        val command = when {
            fluidCanFlowThere(nextStateFluid, targetBlock) -> UpdateCommand(nextStateFluid, x, y)

            currentFluid.isWater() && targetBlock is Block.Lava && targetBlock.state > 0 ->
                UpdateCommand(REACTION_PRIORITY, x, y) { gameWorld.setForeMap(x, y, itemsRepository.getBlockByKey("cobblestone")) }

            currentFluid.isWater() && targetBlock.isLava() ->
                UpdateCommand(REACTION_PRIORITY, x, y) { gameWorld.setForeMap(x, y, itemsRepository.getBlockByKey("obsidian")) }

            currentFluid.isLava() && targetBlock.isWater() ->
                UpdateCommand(REACTION_PRIORITY, x, y) { gameWorld.setForeMap(x, y, itemsRepository.getBlockByKey("stone")) }

            else -> null
        }

        command?.let { updateMap[command.x to command.y] = command }
        return command != null
    }

    private fun flowFluid(x: Int, y: Int) {
        val fluid = gameWorld.getForeMap(x, y) as Block.Fluid
        val stateList = fluidStatesMap[fluid::class] ?: return

        if (flowFluidTo(fluid, x, y + 1, stateList[1])) {
            return
        }

        val below = gameWorld.getForeMap(x, y + 1)
        if (fluid.state > 0 && below is Block.Fluid && below.state > 0) {
            return
        }

        val nextState = getNextStateBlock(fluid) ?: return
        flowFluidTo(fluid, x - 1, y, nextState)
        flowFluidTo(fluid, x + 1, y, nextState)
    }

    private fun regenerateWaterSource(x: Int, y: Int, water: Block.Water): Boolean {
        if (water.state == 0) return false

        val below = gameWorld.getForeMap(x, y + 1)
        val left = gameWorld.getForeMap(x - 1, y)
        val right = gameWorld.getForeMap(x + 1, y)
        if ((below.params.hasCollision || below is Block.Water && below.state == 0) &&
            left is Block.Water && left.state == 0 &&
            right is Block.Water && right.state == 0
        ) {
            updateMap[x to y] = UpdateCommand(fluidStatesMap.getValue(Block.Water::class).first(), x, y)
            return true
        }
        return false
    }

    fun updateFluids(x: Int, y: Int) {
        val block = gameWorld.getForeMap(x, y)
        if (!block.isFluid() || (block.isLava() && updateTick % 2 == 0)) {
            return
        }

        if (block is Block.Water && regenerateWaterSource(x, y, block)) {
            return
        }

        if (drainFluid(x, y)) {
            return
        }

        flowFluid(x, y)
    }

    private fun fluidUpdater() {
        val midScreen = mobController.player.mapX
        val yRange = 16 downTo -16

        for (y in yRange) {
            for (x in 0..<min(gameWorld.width / 2, 32)) {
                updateFluids(midScreen + x, y + mobController.player.middleMapY)
                updateFluids(midScreen - x, y + mobController.player.middleMapY)
            }
        }

        updateMap.values.forEach { command ->
            command.exec()
        }
        updateMap.clear()
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
        override val priority: Int,
        val x: Int,
        val y: Int,
        val command: Runnable,
    ) : PriorityMapValue {

        constructor(fluid: Block.Fluid, x: Int, y: Int) : this(
            // Lower priorities win: reactions, sources, then progressively weaker flows.
            priority = REACTION_PRIORITY + 1 + fluid.state,
            x = x,
            y = y,
            command = Runnable { gameWorld.setForeMap(x, y, fluid) },
        )

        fun exec() = command.run()
    }

    companion object {
        const val FLUID_UPDATE_INTERVAL_SEC = 0.25f
        private const val REACTION_PRIORITY = 1
    }
}
