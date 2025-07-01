package ru.fredboy.cavedroid.game.controller.mob.behavior

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.model.Player

class PlayerMobBehavior : BaseMobBehavior<Player>(
    mobType = Player::class,
) {

    private fun Block?.isHittable() = this != null && !isNone() && params.hitPoints >= 0

    private fun MobWorldAdapter.getTargetBlockWithLayer(x: Int, y: Int): Pair<Block, Layer>? {
        val foregroundBlock = getForegroundBlock(x, y)
        val backgroundBlock = getBackgroundBlock(x, y)

        return when {
            foregroundBlock.isHittable() -> foregroundBlock to Layer.FOREGROUND
            backgroundBlock.isHittable() -> backgroundBlock to Layer.BACKGROUND
            else -> null
        }
    }

    private fun Player.hitBlock(worldAdapter: MobWorldAdapter) {
        if (!isHitting || !isHittingWithDamage) {
            return
        }

        val (targetBlock, targetLayer) = worldAdapter.getTargetBlockWithLayer(cursorX, cursorY)
            ?: run {
                stopHitting()
                return
            }

        if (gameMode == 0) {
            if (blockDamage >= targetBlock.params.hitPoints) {
                val shouldDrop = activeItem.item.let { itemInHand ->
                    val toolLevel = (itemInHand as? Item.Tool)?.level?.takeIf {
                        targetBlock.params.toolType == itemInHand.javaClass
                    } ?: 0

                    toolLevel >= targetBlock.params.toolLevel
                }

                if (activeItem.item.isTool()) {
                    decreaseCurrentItemCount()
                }

                when (targetLayer) {
                    Layer.FOREGROUND -> worldAdapter.destroyForegroundBlock(cursorX, cursorY, shouldDrop)
                    Layer.BACKGROUND -> worldAdapter.destroyBackgroundBlock(cursorX, cursorY, shouldDrop)
                }
                blockDamage = 0f
            }
        } else {
            when (targetLayer) {
                Layer.FOREGROUND -> worldAdapter.destroyForegroundBlock(cursorX, cursorY, false)
                Layer.BACKGROUND -> worldAdapter.destroyBackgroundBlock(cursorX, cursorY, false)
            }
            stopHitting()
        }
    }

    override fun Player.updateMob(worldAdapter: MobWorldAdapter, delta: Float) {
        hitBlock(worldAdapter)

        if (gameMode == 1) {
            return
        }

        val (targetBlock, _) = worldAdapter.getTargetBlockWithLayer(cursorX, cursorY)
            ?.takeIf { isHitting && isHittingWithDamage }
            ?: run {
                blockDamage = 0f
                return@updateMob
            }

        var blockDamageMultiplier = 1f
        (activeItem.item as? Item.Tool)?.let { currentTool ->
            if (currentTool.javaClass == targetBlock.params.toolType &&
                currentTool.level >= targetBlock.params.toolLevel
            ) {
                blockDamageMultiplier = 2f * currentTool.level
            }
            blockDamageMultiplier *= currentTool.blockDamageMultiplier
        }

        if (isHitting && isHittingWithDamage) {
            blockDamage += 60f * delta * blockDamageMultiplier
        }
    }
}
