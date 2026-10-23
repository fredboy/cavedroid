package ru.fredboy.cavedroid.entity.container.model

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.container.abstraction.MobSpawnerAdapter
import kotlin.random.Random

/** A block entity with no inventory or UI, persisted alongside other block entities. */
class Spawner(
    fallbackItem: Item.None,
    var mobKey: String,
    var remainingSeconds: Float = 5f,
) : Container(size = 0, fallbackItem = fallbackItem) {

    override val type get() = Block.Spawner::class

    override fun update(itemByKey: GetItemByKeyUseCase) = Unit

    fun update(delta: Float, coordinates: ContainerCoordinates, adapter: MobSpawnerAdapter) {
        if (!adapter.isPlayerNearby(coordinates)) return
        remainingSeconds = (remainingSeconds - delta).coerceAtLeast(0f)
        if (remainingSeconds > 0f) return

        var spawned = false
        repeat(3) {
            spawned = adapter.trySpawn(coordinates, mobKey) || spawned
        }
        remainingSeconds = if (spawned) Random.nextFloat() * 10f + 10f else 2f
    }
}
