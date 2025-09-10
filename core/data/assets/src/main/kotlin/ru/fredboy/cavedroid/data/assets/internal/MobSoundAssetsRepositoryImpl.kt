package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import ru.fredboy.cavedroid.domain.assets.repository.MobSoundAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MobSoundAssetsRepositoryImpl @Inject constructor() : MobSoundAssetsRepository() {

    private val mobIdleSoundsMap = mutableMapOf<String, List<Sound>>()

    private val mobHitSoundsMap = mutableMapOf<String, List<Sound>>()

    private val mobDeathSoundMap = mutableMapOf<String, List<Sound>>()

    override fun getIdleSound(mobKey: String): Sound? {
        return mobIdleSoundsMap[mobKey]?.takeIf { it.isNotEmpty() }?.random()
    }

    override fun getHitSound(mobKey: String): Sound? {
        return mobHitSoundsMap[mobKey]?.takeIf { it.isNotEmpty() }?.random()
    }

    override fun getDeathSound(mobKey: String): Sound? {
        return mobDeathSoundMap[mobKey]?.takeIf { it.isNotEmpty() }?.random()
    }

    override fun initialize() {
        Gdx.files.internal(MOB_SFX_DIRECTORY).list().forEach { mobDir ->
            mobIdleSoundsMap[mobDir.name()] = mobDir.child(IDLE).list { file ->
                file.extension == "ogg"
            }.map { soundHandle ->
                loadSound(soundHandle)
            }

            mobHitSoundsMap[mobDir.name()] = mobDir.child(HIT).list { file ->
                file.extension == "ogg"
            }.map { soundHandle ->
                loadSound(soundHandle)
            }

            mobDeathSoundMap[mobDir.name()] = mobDir.child(DEATH).list { file ->
                file.extension == "ogg"
            }.map { soundHandle ->
                loadSound(soundHandle)
            }
        }
    }

    override fun dispose() {
        super.dispose()
        mobIdleSoundsMap.clear()
        mobHitSoundsMap.clear()
        mobDeathSoundMap.clear()
    }

    companion object {
        private const val MOB_SFX_DIRECTORY = "sfx/mob"
        private const val HIT = "hit"
        private const val IDLE = "idle"
        private const val DEATH = "death"
    }
}
