package ru.fredboy.cavedroid.gameplay.lighting.tint

import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.game.world.lighting.LightingSystem
import ru.fredboy.cavedroid.game.world.lighting.LightingSystemFactory

class TintLightingSystemFactory : LightingSystemFactory {
    override fun create(gameContextRepository: GameContextRepository): LightingSystem {
        return TintLightingSystem()
    }
}
