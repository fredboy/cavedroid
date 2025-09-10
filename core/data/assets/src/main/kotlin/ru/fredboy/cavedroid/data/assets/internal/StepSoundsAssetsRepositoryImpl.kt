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
        val stepsDir = Gdx.files.internal(STEPS_DIRECTORY)
        val indexFile = Gdx.files.internal(INDEX_FILE)

        indexFile.readString().split("\n").forEach { material ->
            val materialDir = stepsDir.child(material).takeIf { it.exists() } ?: return@forEach
            soundsMap[material] = materialDir.loadAllSounds()
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

        private const val INDEX_FILE = "$STEPS_DIRECTORY/index.txt"
    }
}
