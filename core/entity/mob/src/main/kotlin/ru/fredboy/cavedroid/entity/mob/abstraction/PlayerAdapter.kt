package ru.fredboy.cavedroid.entity.mob.abstraction

import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.Player

interface PlayerAdapter {
    val x: Float
    val y: Float

    val width: Float
    val height: Float

    val cursorX: Float
    val cursorY: Float

    val velocity: Vector2

    val controlMode: Player.ControlMode

    val direction: Direction

    val activeItem: InventoryItem

    val controlVector: Vector2

    val speed: Float

    val gameMode: GameMode

    fun decreaseCurrentItemCount(amount: Int = 1)
}
