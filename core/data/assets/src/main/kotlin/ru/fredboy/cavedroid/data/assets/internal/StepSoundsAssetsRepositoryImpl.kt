package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import ru.fredboy.cavedroid.domain.assets.repository.StepsSoundAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class StepSoundsAssetsRepositoryImpl @Inject constructor() : StepsSoundAssetsRepository() {

    private val soundsMap = mutableMapOf<String, List<Sound>>()

    override fun initialize() {
        Gdx.files.internal(STEPS_DIRECTORY).list().forEach { materialDir ->
            soundsMap[materialDir.name()] = materialDir.list { file ->
                file.extension == "ogg"
            }.map { soundHandle ->
                loadSound(soundHandle)
            }
        }
    }

    override fun getStepSound(material: String): Sound? {
        return soundsMap[material]?.takeIf { it.isNotEmpty() }?.random()
    }

    override fun dispose() {
        super.dispose()
        soundsMap.clear()
    }

    companion object {
        private const val STEPS_DIRECTORY = "sfx/step"
    }
}
