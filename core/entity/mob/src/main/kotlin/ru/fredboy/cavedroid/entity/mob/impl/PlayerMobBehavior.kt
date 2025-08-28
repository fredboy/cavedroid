package ru.fredboy.cavedroid.entity.mob.impl

import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.mob.abstraction.BaseMobBehavior
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.model.Player

class PlayerMobBehavior :
    BaseMobBehavior<Player>(
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

        val (targetBlock, targetLayer) = worldAdapter.getTargetBlockWithLayer(selectedX, selectedY)
            ?: return

        when (gameMode) {
            GameMode.SURVIVAL -> {
                if (blockDamage >= targetBlock.params.hitPoints) {
                    if (activeItem.item.isTool()) {
                        decreaseCurrentItemCount()
                    }

                    when (targetLayer) {
                        Layer.FOREGROUND -> worldAdapter.destroyForegroundBlock(
                            x = selectedX,
                            y = selectedY,
                            shouldDrop = true,
                            destroyedByPlayer = true,
                        )

                        Layer.BACKGROUND -> worldAdapter.destroyBackgroundBlock(
                            x = selectedX,
                            y = selectedY,
                            shouldDrop = true,
                            destroyedByPlayer = true,
                        )
                    }
                    blockDamage = 0f
                }
            }

            GameMode.CREATIVE -> {
                when (targetLayer) {
                    Layer.FOREGROUND -> worldAdapter.destroyForegroundBlock(
                        x = selectedX,
                        y = selectedY,
                        shouldDrop = false,
                        destroyedByPlayer = true,
                    )

                    Layer.BACKGROUND -> worldAdapter.destroyBackgroundBlock(
                        x = selectedX,
                        y = selectedY,
                        shouldDrop = false,
                        destroyedByPlayer = true,
                    )
                }
                stopHitting()
            }
        }
    }

    override fun Player.updateMob(worldAdapter: MobWorldAdapter, playerAdapter: PlayerAdapter, delta: Float) {
        if (isInBed) {
            if (worldAdapter.isDayTime()) {
                isInBed = false
            } else {
                controlVector.setZero()
                velocity.set(0f, 0f)
                return
            }
        }

        hitBlock(worldAdapter)
        updateSight()
        if (checkForAutojump()) {
            jump()
        }

        if (holdCursor) {
            cursorX = position.x + cursorToPlayer.x
            cursorY = position.y + cursorToPlayer.y
        }

        if (gameMode.isCreative()) {
            return
        }

        val (targetBlock, _) = worldAdapter.getTargetBlockWithLayer(selectedX, selectedY)
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
