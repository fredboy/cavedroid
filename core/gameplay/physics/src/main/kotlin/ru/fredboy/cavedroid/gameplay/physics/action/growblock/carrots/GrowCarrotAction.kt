package ru.fredboy.cavedroid.gameplay.physics.action.growblock.carrots

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindGrowBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.IGrowBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.advanceCrop
import javax.inject.Inject

@GameScope
@BindGrowBlockAction(stringKey = "carrots_stage0")
class GrowCarrotStage0Action @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IGrowBlockAction {
    override fun grow(x: Int, y: Int) = advanceCrop(gameWorld, getBlockByKeyUseCase, x, y, "carrots_stage0", "carrots_stage1")
}

@GameScope
@BindGrowBlockAction(stringKey = "carrots_stage1")
class GrowCarrotStage1Action @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IGrowBlockAction {
    override fun grow(x: Int, y: Int) = advanceCrop(gameWorld, getBlockByKeyUseCase, x, y, "carrots_stage1", "carrots_stage2")
}

@GameScope
@BindGrowBlockAction(stringKey = "carrots_stage2")
class GrowCarrotStage2Action @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IGrowBlockAction {
    override fun grow(x: Int, y: Int) = advanceCrop(gameWorld, getBlockByKeyUseCase, x, y, "carrots_stage2", "carrots_stage3")
}
