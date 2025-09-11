package ru.fredboy.cavedroid.gameplay.controls.input.handler.touch

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Timer
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.common.utils.takeIfTrue
import ru.fredboy.cavedroid.domain.assets.repository.FoodSoundAssetsRepository
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.Player
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
class TouchCursorInputHandler @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameContextRepository: GameContextRepository,
    private val mobController: MobController,
    private val gameWorld: GameWorld,
    private val gameWindowsManager: GameWindowsManager,
    private val placeBlockActionMap: Map<String, @JvmSuppressWildcards IPlaceBlockAction>,
    private val useBlockActionMap: Map<String, @JvmSuppressWildcards IUseBlockAction>,
    private val useItemActionMap: Map<String, @JvmSuppressWildcards IUseItemAction>,
    private val useMobActionMap: Map<String, @JvmSuppressWildcards IUseMobAction>,
    private val getTextureRegionByNameUseCase: GetTextureRegionByNameUseCase,
    private val foodSoundAssetsRepository: FoodSoundAssetsRepository,
    private val soundPlayer: SoundPlayer,
    private val projectileController: ProjectileController,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
) : IMouseInputHandler {

    private val player get() = mobController.player

    private var pointer = -1

    private var wasDragged = false

    private val touchDownCoords = Vector2()
    private val touchDownCursorCoords = Vector2()

    private val touchDownPlayerPos = Vector2()

    private var buttonHoldTask: Timer.Task? = null

    private fun cancelHold() {
        buttonHoldTask?.cancel()
        buttonHoldTask = null
    }

    private fun handleHold() {
        cancelHold()

        if (mobController.player.canShootBow()) {
            mobController.player.isPullingBow = true
            return
        }

        val selectedX = mobController.player.selectedX
        val selectedY = mobController.player.selectedY

        if (gameWorld.getForeMap(selectedX, selectedY).isNone() &&
            gameWorld.getBackMap(selectedX, selectedY)
                .isNone()
        ) {
            val item = mobController.player.activeItem.item
            mobController.player.startHitting(false)
            mobController.player.stopHitting()
            if (item is Item.Placeable) {
                placeBlockActionMap.placeToBackgroundAction(
                    item = item,
                    x = selectedX,
                    y = selectedY,
                )
            }
        } else {
            mobController.player.startHitting()
        }
    }

    private fun handleDown() {
        cancelHold()
        buttonHoldTask = object : Timer.Task() {
            override fun run() {
                handleHold()
            }
        }
        Timer.schedule(buttonHoldTask, TOUCH_HOLD_TIME_SEC)
    }

    private fun tryHitMob(): Boolean {
        mobController.mobs.forEach { mob ->
            if (mob.position.dst(player.cursorX, player.cursorY) < 1f) {
                useMobActionMap[mob.params.key]?.perform(mob)?.takeIf { it } ?: player.hitMob(mob)
                return true
            }
        }
        return false
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

    private fun handleUp() {
        val player = mobController.player
        val item = player.activeItem.item

        if (player.isPullingBow) {
            shootBow()
        }

        player.stopHitting()

        if (buttonHoldTask?.isScheduled != true) {
            return
        }

        cancelHold()

        if (wasDragged) {
            return
        }

        if (tryHitMob()) {
            return
        }

        player.startHitting(false)
        player.stopHitting()

        tryUseBlock().takeIfTrue()
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
            }
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
        )?.let { true } ?: false
    }

    override fun checkConditions(action: MouseInputAction): Boolean {
        return applicationContextRepository.isTouch() &&
            gameWindowsManager.currentWindowType == GameWindowType.NONE &&
            (
                (
                    action.actionKey is MouseInputActionKey.Screen &&
                        (
                            action.screenX > applicationContextRepository.getWidth() / 2f &&
                                !action.actionKey.touchUp ||
                                (action.actionKey.pointer == pointer)
                            )
                    ) ||
                    (action.actionKey is MouseInputActionKey.Dragged && action.actionKey.pointer == pointer)
                ) &&
            !action.isInsideHotbar(gameContextRepository, getTextureRegionByNameUseCase)
    }

    override fun handle(action: MouseInputAction) {
        if (action.actionKey is MouseInputActionKey.Screen) {
            if (!action.actionKey.touchUp) {
                pointer = action.actionKey.pointer
                touchDownCoords.set(action.screenX, action.screenY)
                touchDownCursorCoords.set(player.cursorX, player.cursorY)
                touchDownPlayerPos.set(player.position)
                wasDragged = false
                mobController.player.holdCursor = false
                handleTouchDown(action)
            } else {
                handleUp()
                pointer = -1
                mobController.player.holdCursor = true
                mobController.player.cursorToPlayer.set(
                    player.cursorX - player.position.x,
                    player.cursorY - player.position.y,
                )
            }
        } else if (action.actionKey is MouseInputActionKey.Dragged && pointer != -1) {
            wasDragged = true
            handleTouchDown(action)
        }
    }

    private fun handleTouchDown(action: MouseInputAction) {
        moveCursor(action)
        handleDown()
    }

    private fun moveCursor(action: MouseInputAction) {
        val pastSelectedX = player.selectedX
        val pastSelectedY = player.selectedY

        updateCursorPosition(action)
        mobController.limitPlayerCursor()
        setPlayerDirectionToCursor()

        if (player.selectedX != pastSelectedX || player.selectedY != pastSelectedY) {
            player.blockDamage = 0f
        }
    }

    private fun updateCursorPosition(action: MouseInputAction) {
        val moveX = action.screenX - touchDownCoords.x
        val moveY = action.screenY - touchDownCoords.y
        val worldX = touchDownCursorCoords.x + moveX.meters + (player.position.x - touchDownPlayerPos.x)
        val worldY = touchDownCursorCoords.y + moveY.meters + (player.position.y - touchDownPlayerPos.y)

        // when worldX < 0, need to subtract 1 to avoid negative zero
        val fixCycledWorld = if (worldX < 0) 1 else 0

        player.cursorX = worldX - fixCycledWorld
        player.cursorY = worldY

        player.headRotation = getPlayerHeadRotation(worldX, worldY)

        if (worldX.toInt() < player.position.x.toInt()) {
            player.direction = Direction.LEFT
        } else if (worldX.toInt() > player.position.x.toInt()) {
            player.direction = Direction.RIGHT
        }
    }

    private fun getPlayerHeadRotation(mouseWorldX: Float, mouseWorldY: Float): Float {
        val h = mouseWorldX - player.position.x
        val v = mouseWorldY - (player.position.y - player.height / 2f + player.width / 2f)
        val rotation = MathUtils.atan(v / h) * MathUtils.radDeg
        return MathUtils.clamp(rotation, -45f, 45f)
    }

    private fun setPlayerDirectionToCursor() {
        if (player.controlMode != Player.ControlMode.CURSOR) {
            return
        }

        if (player.cursorX < player.position.x) {
            player.direction = Direction.LEFT
        } else {
            player.direction = Direction.RIGHT
        }
    }

    companion object {
        private const val TAG = "TouchWorldInputHandler"
        private const val TOUCH_HOLD_TIME_SEC = 0.5f
    }
}
