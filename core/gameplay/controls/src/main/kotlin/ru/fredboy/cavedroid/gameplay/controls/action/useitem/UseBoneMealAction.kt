package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import javax.inject.Inject

// TODO: collapse this duplication with the GrowSapling*Action classes once the
//  planned action-module refactor lands; for now the controls module can't
//  depend on physics, so the growth shapes are mirrored here.
@GameScope
@BindUseItemAction(UseBoneMealAction.ACTION_KEY)
class UseBoneMealAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int): Boolean {
        val key = gameWorld.getForeMap(x, y).params.key

        when (key) {
            in SAPLING_KEYS -> {
                if (MathUtils.randomBoolean(GROW_CHANCE)) {
                    when (key) {
                        "sapling_oak" -> tryGrowSmallTree(x, y, "log_oak", "leaves_oak")
                        "sapling_birch" -> tryGrowSmallTree(x, y, "log_birch", "leaves_birch")
                        "sapling_spruce" -> tryGrowSpruce(x, y)
                    }
                }
            }
            in WHEAT_GROWING_KEYS -> advanceWheat(x, y, key)
            "grass" -> if (!scatterFlowers(x, y)) return false
            else -> return false
        }

        mobController.player.decreaseCurrentItemCount()
        return true
    }

    private fun advanceWheat(x: Int, y: Int, currentKey: String) {
        val nextStage = WHEAT_NEXT[currentKey] ?: return
        gameWorld.setForeMap(x, y, getBlockByKeyUseCase[nextStage])
    }

    private fun scatterFlowers(x: Int, y: Int): Boolean {
        var placed = 0
        repeat(FLOWER_ATTEMPTS) {
            val dx = MathUtils.random(-FLOWER_RADIUS, FLOWER_RADIUS)
            val dy = MathUtils.random(-1, 1)
            val gx = x + dx
            val gy = y + dy
            if (gameWorld.getForeMap(gx, gy).params.key != "grass") return@repeat

            val above = gameWorld.getForeMap(gx, gy - 1)
            if (!above.params.replaceable || above.isFluid() || !above.isNone()) return@repeat

            val flowerKey = FLOWER_KEYS.random()
            gameWorld.setForeMap(gx, gy - 1, getBlockByKeyUseCase[flowerKey])
            placed++
        }
        return placed > 0
    }

    private fun tryGrowSmallTree(x: Int, y: Int, logKey: String, leavesKey: String): Boolean {
        if (gameWorld.getForeMap(x, y + 1).params.key !in SOIL_KEYS) {
            return false
        }

        for (ix in x - 1..x + 1) {
            for (iy in y - 5..y) {
                if (ix == x && iy == y) continue
                if (!gameWorld.getForeMap(ix, iy).params.replaceable) {
                    return false
                }
            }
        }

        gameWorld.resetForeMap(x, y)

        val leaves = getBlockByKeyUseCase[leavesKey]
        val log = getBlockByKeyUseCase[logKey]

        for (iy in y downTo y - 4) {
            gameWorld.setBackMap(x, iy, log)
        }

        gameWorld.setBackMap(x, y - 5, leaves)
        gameWorld.setForeMap(x, y - 5, leaves)

        for (ix in x - 1..x + 1) {
            for (iy in y - 4..y - 3) {
                if (ix != x) {
                    gameWorld.setBackMap(ix, iy, leaves)
                }
                gameWorld.setForeMap(ix, iy, leaves)
            }
        }

        return true
    }

    private fun tryGrowSpruce(x: Int, y: Int): Boolean {
        if (gameWorld.getForeMap(x, y + 1).params.key !in SOIL_KEYS) {
            return false
        }

        for (ix in x - 2..x + 2) {
            for (iy in y - 7..y) {
                if (ix == x && iy == y) continue
                if (!gameWorld.getForeMap(ix, iy).params.replaceable) {
                    return false
                }
            }
        }

        gameWorld.resetForeMap(x, y)

        val leaves = getBlockByKeyUseCase["leaves_spruce"]
        val log = getBlockByKeyUseCase["log_spruce"]

        for (iy in y downTo y - 6) {
            gameWorld.setBackMap(x, iy, log)
        }

        gameWorld.setBackMap(x, y - 7, leaves)
        gameWorld.setForeMap(x, y - 7, leaves)

        for (ix in x - 1..x + 1) {
            val iy = y - 6
            if (ix != x) {
                gameWorld.setBackMap(ix, iy, leaves)
            }
            gameWorld.setForeMap(ix, iy, leaves)
        }

        for (iy in 1..2) {
            for (ix in x - iy..x + iy) {
                if (ix != x) {
                    gameWorld.setBackMap(ix, y - 5 + iy, leaves)
                }
                gameWorld.setForeMap(ix, y - 5 + iy, leaves)
            }
        }

        return true
    }

    companion object {
        const val ACTION_KEY = "use_bone_meal_action"

        private const val GROW_CHANCE = 0.45f
        private const val FLOWER_ATTEMPTS = 12
        private const val FLOWER_RADIUS = 3
        private val SAPLING_KEYS = setOf("sapling_oak", "sapling_birch", "sapling_spruce")
        private val SOIL_KEYS = setOf("dirt", "grass", "grass_snowed")
        private val FLOWER_KEYS = listOf("dandelion", "rose", "tallgrass", "tallgrass", "tallgrass")
        private val WHEAT_NEXT = mapOf(
            "wheat_stage0" to "wheat_stage1",
            "wheat_stage1" to "wheat_stage2",
            "wheat_stage2" to "wheat_stage3",
            "wheat_stage3" to "wheat_stage4",
            "wheat_stage4" to "wheat_stage5",
            "wheat_stage5" to "wheat_stage6",
        )
        private val WHEAT_GROWING_KEYS = WHEAT_NEXT.keys
    }
}
