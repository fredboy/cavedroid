package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import ru.fredboy.cavedroid.domain.assets.repository.FoodSoundAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FoodSoundAssetsRepositoryImpl @Inject constructor() : FoodSoundAssetsRepository() {

    private var foodSounds: List<Sound>? = null

    override fun initialize() {
        foodSounds = Gdx.files.internal(DROP_POP_PATH).list { file ->
            file.extension == "ogg"
        }.map { soundHandle ->
            loadSound(soundHandle)
        }
    }

    override fun getFoodSound(): Sound? {
        return foodSounds?.takeIf { it.isNotEmpty() }?.random()
    }

    override fun dispose() {
        super.dispose()
        foodSounds = null
    }

    companion object {
        private const val DROP_POP_PATH = "sfx/food"
    }
}
