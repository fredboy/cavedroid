package ru.fredboy.cavedroid.game.world.lighting

import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository

interface LightingSystemFactory {
    fun create(gameContextRepository: GameContextRepository): LightingSystem
}
