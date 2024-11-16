package ru.fredboy.cavedroid.entity.container.abstraction

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.entity.container.model.Container

interface ContainerFactory {
    fun createContainer(type: Block.Container): Container
}