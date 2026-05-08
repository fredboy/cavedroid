package ru.fredboy.cavedroid.gdx.di

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.gdx.CaveDroidSoundPlayer
import javax.inject.Singleton

@Module
abstract class ApplicationModule {

    @Binds
    internal abstract fun bindSoundPlayer(impl: CaveDroidSoundPlayer): SoundPlayer

    companion object {

        @Provides
        @Singleton
        fun provideMenuSkin(): Skin = Skin(Gdx.files.internal("skin/skin.json"))
            .apply {
                atlas.textures.forEach { texture ->
                    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
                }

                getAll(BitmapFont::class.java).forEach { entry ->
                    entry.value.data.setScale(1f)
                }
            }
    }
}
