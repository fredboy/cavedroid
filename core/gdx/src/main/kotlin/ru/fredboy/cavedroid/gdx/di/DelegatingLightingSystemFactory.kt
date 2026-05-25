package ru.fredboy.cavedroid.gdx.di

import ru.fredboy.cavedroid.common.CaveDroidConstants.PreferenceKeys
import ru.fredboy.cavedroid.common.api.PreferencesStore
import ru.fredboy.cavedroid.domain.configuration.model.LightingBackend
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.game.world.lighting.LightingSystem
import ru.fredboy.cavedroid.game.world.lighting.LightingSystemFactory

class DelegatingLightingSystemFactory(
    private val preferencesStore: PreferencesStore,
    private val legacy: LightingSystemFactory,
    private val bfs: LightingSystemFactory,
) : LightingSystemFactory {

    override fun create(gameContextRepository: GameContextRepository): LightingSystem {
        val backend = LightingBackend.fromName(preferencesStore.getPreference(PreferenceKeys.LIGHTING_BACKEND))
        val delegate = when (backend) {
            LightingBackend.BFS -> bfs
            LightingBackend.LEGACY -> legacy
        }
        return delegate.create(gameContextRepository)
    }
}
