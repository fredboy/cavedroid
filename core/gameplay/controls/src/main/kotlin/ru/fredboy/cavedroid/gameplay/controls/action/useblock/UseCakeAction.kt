package ru.fredboy.cavedroid.gameplay.controls.action.useblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseBlockAction
import javax.inject.Inject

abstract class UseCakeAction(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IUseBlockAction {

    override fun perform(block: Block, x: Int, y: Int): Boolean {
        if (mobController.player.foodLevel >= Player.MAX_FOOD_LEVEL) {
            return false
        }
        mobController.player.eat(HEAL, SATURATION)

        val nextCake = CAKE_NEXT[block.params.key]?.let { key ->
            getBlockByKeyUseCase[key]
        }

        if (nextCake != null) {
            gameWorld.setForeMap(x, y, nextCake)
        } else {
            gameWorld.resetForeMap(x, y)
        }

        return true
    }

    companion object {
        private const val HEAL = 2
        private const val SATURATION = 0.4f

        private val CAKE_NEXT = mapOf(
            "cake_0" to "cake_1",
            "cake_1" to "cake_2",
            "cake_2" to "cake_3",
            "cake_3" to "cake_4",
            "cake_4" to "cake_5",
            "cake_5" to "cake_6",
        )
    }
}

@GameScope
@BindUseBlockAction(stringKey = "cake_0")
class UseCake0Action @Inject constructor(
    gameWorld: GameWorld,
    mobController: MobController,
    getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : UseCakeAction(
    gameWorld = gameWorld,
    mobController = mobController,
    getBlockByKeyUseCase = getBlockByKeyUseCase,
)

@GameScope
@BindUseBlockAction(stringKey = "cake_1")
class UseCake1Action @Inject constructor(
    gameWorld: GameWorld,
    mobController: MobController,
    getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : UseCakeAction(
    gameWorld = gameWorld,
    mobController = mobController,
    getBlockByKeyUseCase = getBlockByKeyUseCase,
)

@GameScope
@BindUseBlockAction(stringKey = "cake_2")
class UseCake2Action @Inject constructor(
    gameWorld: GameWorld,
    mobController: MobController,
    getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : UseCakeAction(
    gameWorld = gameWorld,
    mobController = mobController,
    getBlockByKeyUseCase = getBlockByKeyUseCase,
)

@GameScope
@BindUseBlockAction(stringKey = "cake_3")
class UseCake3Action @Inject constructor(
    gameWorld: GameWorld,
    mobController: MobController,
    getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : UseCakeAction(
    gameWorld = gameWorld,
    mobController = mobController,
    getBlockByKeyUseCase = getBlockByKeyUseCase,
)

@GameScope
@BindUseBlockAction(stringKey = "cake_4")
class UseCake4Action @Inject constructor(
    gameWorld: GameWorld,
    mobController: MobController,
    getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : UseCakeAction(
    gameWorld = gameWorld,
    mobController = mobController,
    getBlockByKeyUseCase = getBlockByKeyUseCase,
)

@GameScope
@BindUseBlockAction(stringKey = "cake_5")
class UseCake5Action @Inject constructor(
    gameWorld: GameWorld,
    mobController: MobController,
    getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : UseCakeAction(
    gameWorld = gameWorld,
    mobController = mobController,
    getBlockByKeyUseCase = getBlockByKeyUseCase,
)

@GameScope
@BindUseBlockAction(stringKey = "cake_6")
class UseCake6Action @Inject constructor(
    gameWorld: GameWorld,
    mobController: MobController,
    getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : UseCakeAction(
    gameWorld = gameWorld,
    mobController = mobController,
    getBlockByKeyUseCase = getBlockByKeyUseCase,
)
