package ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Timer
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.takeIfTrue
import ru.fredboy.cavedroid.domain.assets.repository.BlockActionSoundAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.FoodSoundAssetsRepository
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.projectile.model.Projectile
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.projectile.ProjectileController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.placeToBackgroundAction
import ru.fredboy.cavedroid.gameplay.controls.action.placeToForegroundAction
import ru.fredboy.cavedroid.gameplay.controls.action.placeblock.IPlaceBlockAction
import ru.fredboy.cavedroid.gameplay.controls.action.useblock.IUseBlockAction
import ru.fredboy.cavedroid.gameplay.controls.action.useitem.IUseItemAction
import ru.fredboy.cavedroid.gameplay.controls.action.usemob.IUseMobAction
import ru.fredboy.cavedroid.gameplay.controls.input.IMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.isInsideHotbar
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class UseItemMouseInputHandler @Inject constructor(
    private val gameContextRepository: GameContextRepository,
    private val mobController: MobController,
    private val useItemActionMap: Map<String, @JvmSuppressWildcards IUseItemAction>,
    private val placeBlockActionMap: Map<String, @JvmSuppressWildcards IPlaceBlockAction>,
    private val useBlockActionMap: Map<String, @JvmSuppressWildcards IUseBlockAction>,
    private val useMobActionMap: Map<String, @JvmSuppressWildcards IUseMobAction>,
    private val gameWindowsManager: GameWindowsManager,
    private val gameWorld: GameWorld,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val applicationContextRepository: ApplicationContextRepository,
    private val foodSoundAssetsRepository: FoodSoundAssetsRepository,
    private val soundPlayer: SoundPlayer,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
    private val projectileController: ProjectileController,
    private val blockActionSoundAssetsRepository: BlockActionSoundAssetsRepository,
) : IMouseInputHandler {

    private var buttonHoldTask: Timer.Task? = null

    override fun checkConditions(action: MouseInputAction): Boolean {
        return !applicationContextRepository.isTouch() &&
            (
                buttonHoldTask?.isScheduled == true ||
                    !action.isInsideHotbar(gameContextRepository, textureRegions) &&
                    gameWindowsManager.currentWindowType == GameWindowType.NONE &&
                    action.actionKey is MouseInputActionKey.Right
                )
    }

    private fun cancelHold() {
        buttonHoldTask?.cancel()
        buttonHoldTask = null
    }

    private fun handleHold() {
        cancelHold()

        val player = mobController.player
        val item = player.activeItem.item
        player.startHitting(false)
        player.stopHitting()

        if (item is Item.Placeable) {
            placeBlockActionMap.placeToBackgroundAction(
                item = item,
                x = player.selectedX,
                y = player.selectedY,
            )
        }
    }

    private fun handleDown() {
        if (mobController.player.canShootBow()) {
            mobController.player.isPullingBow = true
            return
        }

        cancelHold()
        buttonHoldTask = object : Timer.Task() {
            override fun run() {
                handleHold()
            }
        }
        Timer.schedule(buttonHoldTask, TOUCH_HOLD_TIME_SEC)
    }

    private fun tryUseBlock(): Boolean {
        val block = gameWorld.getForeMap(mobController.player.selectedX, mobController.player.selectedY)
            .takeIf { !it.isNone() }
            ?: gameWorld.getBackMap(mobController.player.selectedX, mobController.player.selectedY)
                .takeIf { !it.isNone() }
            ?: return false

        return useBlockActionMap[block.params.key]?.perform(
            block = block,
            x = mobController.player.selectedX,
            y = mobController.player.selectedY,
        )?.let {
            block.params.actionSoundKey?.let { key ->
                blockActionSoundAssetsRepository.getBlockActionSound(key)?.let { sound ->
                    soundPlayer.playSoundAtPosition(
                        sound = sound,
                        soundX = mobController.player.cursorX,
                        soundY = mobController.player.cursorY,
                        playerX = mobController.player.position.x,
                        playerY = mobController.player.position.y,
                    )
                }
            }
            true
        } ?: false
    }

    private fun tryUseMob(): Boolean {
        val mob = mobController.mobs.firstOrNull { mob ->
            mob.hitbox.contains(mobController.player.cursorX, mobController.player.cursorY) &&
                mobController.player.position.cpy().sub(mob.position).len() <= MOB_HIT_RANGE
        } ?: return false

        return useMobActionMap[mob.params.key]?.perform(mob) ?: false
    }

    private fun playFoodSound() {
        val sound = foodSoundAssetsRepository.getFoodSound() ?: return
        soundPlayer.playSoundAtPosition(
            sound = sound,
            soundX = mobController.player.position.x,
            soundY = mobController.player.position.x,
            playerX = mobController.player.position.x,
            playerY = mobController.player.position.x,
        )
    }

    private fun handleUp() {
        val player = mobController.player
        val item = player.activeItem.item
        player.isPullingBow = false
        cancelHold()

        player.startHitting(false)
        player.stopHitting()

        tryUseMob().takeIfTrue()
            ?: tryUseBlock().takeIfTrue()
            ?: (item as? Item.Placeable)?.let {
                placeBlockActionMap.placeToForegroundAction(
                    item = item,
                    x = player.selectedX,
                    y = player.selectedY,
                )
            }?.takeIfTrue()
            ?: (item as? Item.Usable)?.let {
                useItemActionMap[item.useActionKey]?.perform(item, player.selectedX, player.selectedY)
                    ?: run {
                        Gdx.app.error(TAG, "use item action ${item.useActionKey} not found")
                        false
                    }
            }?.takeIfTrue()
            ?: (item as? Item.Food)?.let {
                if (player.health < player.maxHealth) {
                    playFoodSound()
                    player.heal(item.heal)
                    player.decreaseCurrentItemCount()
                    true
                } else {
                    false
                }
            }?.takeIfTrue()
    }

    private fun shootBow() {
        projectileController.addProjectile(
            projectile = Projectile(
                item = getItemByKeyUseCase["arrow"],
                damage = 1 + (mobController.player.bowState * 3),
                width = 1f,
                height = 0.25f,
                dropOnGround = true,
            ),
            x = mobController.player.position.x + mobController.player.direction.basis,
            y = mobController.player.position.y - mobController.player.height / 3f,
            velocity = Vector2.X.cpy()
                .setAngleDeg(mobController.player.headRotation + 180f * (1 - mobController.player.direction.index))
                .scl(300f * ((mobController.player.bowState.toFloat() + 1f) / 3f)),
        )
        mobController.player.isPullingBow = false
        mobController.player.decreaseArrows()
        mobController.player.decreaseCurrentItemCount()
    }

    override fun handle(action: MouseInputAction) {
        if (action.actionKey !is MouseInputActionKey.Right) {
            if (buttonHoldTask?.isScheduled == true) {
                cancelHold()
            }
            return
        }

        if (action.actionKey.touchUp) {
            if (buttonHoldTask?.isScheduled == true) {
                handleUp()
            } else if (mobController.player.isPullingBow) {
                shootBow()
            }
        } else if (!action.actionKey.touchUp) {
            handleDown()
        }
    }

    companion object {
        private const val TAG = "UseItemMouseInputActionHandler"
        private const val TOUCH_HOLD_TIME_SEC = 0.5f

        private const val MOB_HIT_RANGE = 3f
    }
}
