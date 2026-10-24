package ru.fredboy.cavedroid.gameplay.physics.action.growblock.potatoes

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindGrowBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.IGrowBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.advanceCrop
import javax.inject.Inject

@GameScope
@BindGrowBlockAction(stringKey = "potatoes_stage0")
class GrowPotatoStage0Action @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IGrowBlockAction {
    override fun grow(x: Int, y: Int) = advanceCrop(gameWorld, getBlockByKeyUseCase, x, y, "potatoes_stage0", "potatoes_stage1")
}

@GameScope
@BindGrowBlockAction(stringKey = "potatoes_stage1")
class GrowPotatoStage1Action @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IGrowBlockAction {
    override fun grow(x: Int, y: Int) = advanceCrop(gameWorld, getBlockByKeyUseCase, x, y, "potatoes_stage1", "potatoes_stage2")
}

@GameScope
@BindGrowBlockAction(stringKey = "potatoes_stage2")
class GrowPotatoStage2Action @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IGrowBlockAction {
    override fun grow(x: Int, y: Int) = advanceCrop(gameWorld, getBlockByKeyUseCase, x, y, "potatoes_stage2", "potatoes_stage3")
}
