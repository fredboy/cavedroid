package ru.fredboy.cavedroid.ux.controls.input.handler.mouse

import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.common.utils.bl
import ru.fredboy.cavedroid.common.utils.px
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByIndexUseCase
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.TooltipManager
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.ux.controls.input.IMouseInputHandler
import ru.fredboy.cavedroid.ux.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.ux.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.ux.controls.input.annotation.BindMouseInputHandler
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class CursorMouseInputHandler @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val mobController: MobController,
    private val gameWorld: GameWorld,
    private val gameWindowsManager: GameWindowsManager,
    private val tooltipManager: TooltipManager,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getItemByIndexUseCase: GetItemByIndexUseCase,
) : IMouseInputHandler {

    private val player get() = mobController.player

    private val creativeInventoryTexture get() = requireNotNull(textureRegions["creative"])

    private val Block.isAutoselectable
        get() = !isNone() && params.hasCollision

    private fun GameWorld.isCurrentBlockAutoselectable() =
        getForeMap(player.cursorX, player.cursorY).isAutoselectable

    private fun setPlayerDirectionToCursor() {
        if (player.controlMode != Player.ControlMode.CURSOR) {
            return
        }

        if (player.cursorX.px + 8 < player.x + player.width / 2) {
            player.direction = Direction.LEFT
        } else {
            player.direction = Direction.RIGHT
        }
    }

    private fun handleWalkTouch() {
        player.cursorX = player.mapX + player.direction.basis
        player.cursorY = player.upperMapY
        player.headRotation = 0f

        for (i in 1..2) {
            if (gameWorld.isCurrentBlockAutoselectable()) {
                break
            }
            player.cursorY++
        }

        if (!gameWorld.isCurrentBlockAutoselectable()) {
            player.cursorX -= player.direction.basis
        }
    }

    private fun getPlayerHeadRotation(mouseWorldX: Float, mouseWorldY: Float): Float {
        val h = mouseWorldX - (player.x + player.width / 2)
        val v = mouseWorldY - player.y

        return MathUtils.atan(v / h) * MathUtils.radDeg
    }

    private fun handleMouse(action: MouseInputAction) {
        val worldX = action.screenX + action.cameraViewport.x
        val worldY = action.screenY + action.cameraViewport.y

        // when worldX < 0, need to subtract 1 to avoid negative zero
//        val fixCycledWorld = if (worldX < 0) 1 else 0

        player.cursorX = worldX.bl - 0
        player.cursorY = worldY.bl

        player.headRotation = getPlayerHeadRotation(worldX, worldY)

        if (worldX < player.x + player.width / 2) {
            player.direction = Direction.LEFT
        } else {
            player.direction = Direction.RIGHT
        }
    }

    private fun getCreativeTooltip(action: MouseInputAction): String? {
        val creativeTexture = creativeInventoryTexture
        val xOnGrid = (action.screenX - (action.cameraViewport.width / 2 - creativeTexture.regionWidth / 2 +
                GameWindowsConfigs.Creative.itemsGridMarginLeft)) /
                GameWindowsConfigs.Creative.itemsGridColWidth
        val yOnGrid = (action.screenY - (action.cameraViewport.height / 2 - creativeTexture.regionHeight / 2 +
                GameWindowsConfigs.Creative.itemsGridMarginTop)) /
                GameWindowsConfigs.Creative.itemsGridRowHeight

        if (xOnGrid < 0 || xOnGrid >= GameWindowsConfigs.Creative.itemsInRow ||
            yOnGrid < 0 || yOnGrid >= GameWindowsConfigs.Creative.itemsInCol) {
            return null
        }

        val itemIndex = (gameWindowsManager.creativeScrollAmount * GameWindowsConfigs.Creative.itemsInRow +
                (xOnGrid.toInt() + yOnGrid.toInt() * GameWindowsConfigs.Creative.itemsInRow))
        val item = getItemByIndexUseCase[itemIndex]

        return item.params.name
    }

    override fun checkConditions(action: MouseInputAction): Boolean {
        return action.actionKey is MouseInputActionKey.None
    }

    override fun handle(action: MouseInputAction) {
        val pastCursorX = player.cursorX
        val pastCursorY = player.cursorY

        when {
            player.controlMode == Player.ControlMode.WALK && applicationContextRepository.isTouch() -> handleWalkTouch()
            !applicationContextRepository.isTouch() -> handleMouse(action)
        }

        mobController.checkPlayerCursorBounds()

        if (player.controlMode == Player.ControlMode.WALK && applicationContextRepository.isTouch()) {
            setPlayerDirectionToCursor()
        }

        if (player.cursorX != pastCursorX || player.cursorY != pastCursorY) {
            player.blockDamage = 0f
        }

        if (gameWindowsManager.currentWindowType == GameWindowType.CREATIVE_INVENTORY) {
            tooltipManager.showMouseTooltip(getCreativeTooltip(action).orEmpty())
        }
    }

}