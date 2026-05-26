package ru.fredboy.cavedroid.gameplay.lighting.box2d

import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.game.world.lighting.LightingSystem
import ru.fredboy.cavedroid.game.world.lighting.LightingSystemFactory

@Deprecated(
    "Replaced by BfsLightingSystemFactory. Kept only for parity with older saves; not maintained.",
    level = DeprecationLevel.WARNING,
)
class Box2dLightingSystemFactory : LightingSystemFactory {
    @Suppress("DEPRECATION")
    override fun create(gameContextRepository: GameContextRepository): LightingSystem {
        return Box2dLightingSystem(gameContextRepository)
    }
}
