package ru.fredboy.cavedroid.gameplay.physics.action.growblock.wheat

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindGrowBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.IGrowBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.advanceCrop
import javax.inject.Inject

@GameScope
@BindGrowBlockAction(stringKey = "wheat_stage0")
class GrowWheatStage0Action @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IGrowBlockAction {
    override fun grow(x: Int, y: Int) = advanceCrop(gameWorld, getBlockByKeyUseCase, x, y, "wheat_stage0", "wheat_stage1")
}

@GameScope
@BindGrowBlockAction(stringKey = "wheat_stage1")
class GrowWheatStage1Action @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IGrowBlockAction {
    override fun grow(x: Int, y: Int) = advanceCrop(gameWorld, getBlockByKeyUseCase, x, y, "wheat_stage1", "wheat_stage2")
}

@GameScope
@BindGrowBlockAction(stringKey = "wheat_stage2")
class GrowWheatStage2Action @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IGrowBlockAction {
    override fun grow(x: Int, y: Int) = advanceCrop(gameWorld, getBlockByKeyUseCase, x, y, "wheat_stage2", "wheat_stage3")
}

@GameScope
@BindGrowBlockAction(stringKey = "wheat_stage3")
class GrowWheatStage3Action @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IGrowBlockAction {
    override fun grow(x: Int, y: Int) = advanceCrop(gameWorld, getBlockByKeyUseCase, x, y, "wheat_stage3", "wheat_stage4")
}

@GameScope
@BindGrowBlockAction(stringKey = "wheat_stage4")
class GrowWheatStage4Action @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IGrowBlockAction {
    override fun grow(x: Int, y: Int) = advanceCrop(gameWorld, getBlockByKeyUseCase, x, y, "wheat_stage4", "wheat_stage5")
}

@GameScope
@BindGrowBlockAction(stringKey = "wheat_stage5")
class GrowWheatStage5Action @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IGrowBlockAction {
    override fun grow(x: Int, y: Int) = advanceCrop(gameWorld, getBlockByKeyUseCase, x, y, "wheat_stage5", "wheat_stage6")
}
