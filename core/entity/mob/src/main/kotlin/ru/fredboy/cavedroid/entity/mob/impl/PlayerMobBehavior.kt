package ru.fredboy.cavedroid.entity.mob.impl

import box2dLight.publicUpdate
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.TimeUtils
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.domain.assets.repository.StepsSoundAssetsRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.mob.abstraction.BaseMobBehavior
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.ProjectileAdapter
import ru.fredboy.cavedroid.entity.mob.model.Player

class PlayerMobBehavior(
    private val soundPlayer: SoundPlayer,
    private val stepsSoundAssetsRepository: StepsSoundAssetsRepository,
) : BaseMobBehavior<Player>(
    mobType = Player::class,
) {

    private var creativeDestroyBlockMs = 0L

    private var lastBreakSoundPlayedDelta = 0f

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

    private fun Player.playBlockHitSound(targetBlock: Block, pitch: Float = 2f) {
        targetBlock.params.material?.name?.lowercase()?.also { material ->
            val sound = stepsSoundAssetsRepository.getStepSound(material) ?: return@also
            soundPlayer.playSoundAtPosition(
                sound = sound,
                soundX = cursorX,
                soundY = cursorY,
                playerX = position.x,
                playerY = position.y,
                pitch = MathUtils.clamp(pitch, 0.5f, 2f),
            )
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
                if (lastBreakSoundPlayedDelta >= BREAK_SOUND_INTERVAL || blockDamage >= targetBlock.params.hitPoints) {
                    lastBreakSoundPlayedDelta = 0f
                    playBlockHitSound(targetBlock, blockDamage / targetBlock.params.hitPoints + 1f)
                }

                if (blockDamage >= targetBlock.params.hitPoints) {
                    if (activeItem.item.isTool()) {
                        durateActiveDurable()
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
                if (TimeUtils.timeSinceMillis(creativeDestroyBlockMs) >= CREATIVE_DESTROY_TIMEOUT_MS) {
                    playBlockHitSound(targetBlock, 2f)
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
                    creativeDestroyBlockMs = TimeUtils.millis()
                }
            }
        }
    }

    override fun Player.updateMob(
        worldAdapter: MobWorldAdapter,
        playerAdapter: PlayerAdapter,
        projectileAdapter: ProjectileAdapter,
        delta: Float,
    ) {
        sight?.publicUpdate()

        if (isPullingBow && !canShootBow()) {
            isPullingBow = false
        }

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
        if (checkForAutojump(worldAdapter)) {
            jump()
        }

        if (holdCursor) {
            cursorX = position.x + cursorToPlayer.x
            cursorY = position.y + cursorToPlayer.y
            rayCastCursor(worldAdapter)
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
            val waterFactor = worldAdapter.getForegroundBlock(mapX, upperMapY)
                .let { it.isWater() && it.getRectangle(mapX, upperMapY).overlaps(hitbox) }
                .ifTrue { 0.2f } ?: 1f
            blockDamage += 60f * delta * blockDamageMultiplier * waterFactor
            lastBreakSoundPlayedDelta += delta
        }
    }

    companion object {
        private const val CREATIVE_DESTROY_TIMEOUT_MS = 500L

        private const val BREAK_SOUND_INTERVAL = 0.2f
    }
}
