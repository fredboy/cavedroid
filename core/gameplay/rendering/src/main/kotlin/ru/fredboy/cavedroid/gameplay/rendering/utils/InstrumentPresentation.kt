package ru.fredboy.cavedroid.gameplay.rendering.utils

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

/** Update catalog sprites once before drawing so every inventory and world view agrees. */
@GameScope
class InstrumentPresentation @Inject constructor(
    items: ItemsRepository,
    private val world: GameWorld,
    private val player: PlayerAdapter,
) {
    private val instruments = items.getAllItems().filterIsInstance<Item.Instrument>()
    private val compass = InstrumentNeedle()

    fun update(delta: Float) {
        val spawn = world.worldSpawn
        val clockTurns = InstrumentDial.clock(world.currentGameTime, GameWorld.DAY_DURATION_SEC.toFloat())
        val compassTurns = spawn?.let {
            compass.update(
                InstrumentDial.compass(player.x, player.y, it.x, it.y, if (world.isInfinite) 0f else world.width.toFloat()),
                delta,
            )
        } ?: 0f
        instruments.forEach { item ->
            val turns = when (item.kind) {
                Item.Instrument.Kind.CLOCK -> clockTurns
                Item.Instrument.Kind.COMPASS -> compassTurns
            }
            item.frame = InstrumentDial.frame(turns, item.stateSprites.size)
        }
    }
}
