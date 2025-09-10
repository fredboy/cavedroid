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
        val mobSfxDir = Gdx.files.internal(MOB_SFX_DIRECTORY)
        val indexFile = Gdx.files.internal(INDEX_FILE)

        indexFile.readString().split("\n").forEach { mobKey ->
            val mobDir = mobSfxDir.child(mobKey).takeIf { it.exists() } ?: return@forEach

            mobIdleSoundsMap[mobKey] = mobDir.child(IDLE).loadAllSounds()
            mobHitSoundsMap[mobKey] = mobDir.child(HIT).loadAllSounds()
            mobDeathSoundMap[mobKey] = mobDir.child(DEATH).loadAllSounds()
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
        private const val INDEX_FILE = "$MOB_SFX_DIRECTORY/index.txt"
        private const val HIT = "hit"
        private const val IDLE = "idle"
        private const val DEATH = "death"
    }
}
