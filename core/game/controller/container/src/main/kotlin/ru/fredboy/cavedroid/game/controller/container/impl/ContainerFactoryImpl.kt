package ru.fredboy.cavedroid.game.controller.container.impl

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory
import ru.fredboy.cavedroid.entity.container.model.Chest
import ru.fredboy.cavedroid.entity.container.model.Container
import ru.fredboy.cavedroid.entity.container.model.Furnace
import ru.fredboy.cavedroid.entity.container.model.MineshaftLoot
import ru.fredboy.cavedroid.entity.container.model.MonsterRoomLoot
import ru.fredboy.cavedroid.entity.container.model.Spawner
import ru.fredboy.cavedroid.entity.container.model.VillageLoot
import javax.inject.Inject

@GameScope
internal class ContainerFactoryImpl @Inject constructor(
    private val getFallbackItemUseCase: GetFallbackItemUseCase,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
) : ContainerFactory {

    override fun createContainer(type: Block.Container, seed: Long): Container {
        return when (type) {
            is Block.Furnace -> createFurnace()
            is Block.Chest -> createChest().apply {
                val loot = when (type.lootTable) {
                    "mineshaft" -> MineshaftLoot.roll(seed)
                    "monster_room" -> MonsterRoomLoot.roll(seed)
                    "village" -> VillageLoot.roll(seed)
                    else -> emptyList()
                }
                loot.forEach { (key, amount) ->
                    inventory.pickUpItem(getItemByKeyUseCase[key].toInventoryItem(amount, durability = 0))
                }
            }
            is Block.Spawner -> Spawner(getFallbackItemUseCase(), type.mobKey)
        }
    }

    private fun createFurnace(): Furnace {
        return Furnace(
            fallbackItem = getFallbackItemUseCase(),
        )
    }

    private fun createChest(): Chest {
        return Chest(
            fallbackItem = getFallbackItemUseCase(),
        )
    }
}
