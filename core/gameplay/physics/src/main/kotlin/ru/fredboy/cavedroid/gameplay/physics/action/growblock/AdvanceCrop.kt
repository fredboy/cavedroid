package ru.fredboy.cavedroid.gameplay.physics.action.growblock

import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld

private val FARMLAND_KEYS = setOf("farmland", "farmland_moist")

internal fun advanceCrop(
    gameWorld: GameWorld,
    getBlockByKeyUseCase: GetBlockByKeyUseCase,
    x: Int,
    y: Int,
    currentKey: String,
    nextKey: String,
): Boolean {
    if (gameWorld.getForeMap(x, y).params.key != currentKey) return false
    if (gameWorld.getForeMap(x, y + 1).params.key !in FARMLAND_KEYS) return false
    gameWorld.setForeMap(x, y, getBlockByKeyUseCase[nextKey])
    return true
}
