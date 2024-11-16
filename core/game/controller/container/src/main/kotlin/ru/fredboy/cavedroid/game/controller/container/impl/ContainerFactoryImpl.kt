package ru.fredboy.cavedroid.game.controller.container.impl

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory
import ru.fredboy.cavedroid.entity.container.model.Chest
import ru.fredboy.cavedroid.entity.container.model.Container
import ru.fredboy.cavedroid.entity.container.model.Furnace
import javax.inject.Inject

@GameScope
internal class ContainerFactoryImpl @Inject constructor(
    private val getFallbackItemUseCase: GetFallbackItemUseCase,
): ContainerFactory {

    override fun createContainer(type: Block.Container): Container {
        return when (type) {
            is Block.Furnace -> createFurnace()
            is Block.Chest -> createChest()
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