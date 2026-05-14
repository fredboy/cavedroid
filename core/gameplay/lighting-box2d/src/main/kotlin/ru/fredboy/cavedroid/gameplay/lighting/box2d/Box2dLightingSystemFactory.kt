package ru.fredboy.cavedroid.gameplay.lighting.box2d

import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.game.world.lighting.LightingSystem
import ru.fredboy.cavedroid.game.world.lighting.LightingSystemFactory

class Box2dLightingSystemFactory : LightingSystemFactory {
    override fun create(gameContextRepository: GameContextRepository): LightingSystem {
        return Box2dLightingSystem(gameContextRepository)
    }
}
