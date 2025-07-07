package ru.fredboy.cavedroid.game.controller.mob.impl

import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import javax.inject.Inject

@GameScope
class PlayerAdapterImpl @Inject constructor(
    private val mobController: MobController,
) : PlayerAdapter {

    private val player get() = mobController.player

    override val x: Float
        get() = player.position.x

    override val y: Float
        get() = player.position.y

    override val width: Float
        get() = player.width

    override val height: Float
        get() = player.height

    override val cursorX: Float
        get() = player.cursorX.toFloat()

    override val cursorY: Float
        get() = player.cursorY.toFloat()

    override val velocity: Vector2
        get() = player.velocity.get()

    override val controlMode: Player.ControlMode
        get() = player.controlMode

    override val direction: Direction
        get() = player.direction

    override val activeItem: InventoryItem
        get() = player.activeItem

    override val controlVector: Vector2
        get() = player.controlVector

    override fun decreaseCurrentItemCount(amount: Int) {
        player.decreaseCurrentItemCount(amount)
    }
}
