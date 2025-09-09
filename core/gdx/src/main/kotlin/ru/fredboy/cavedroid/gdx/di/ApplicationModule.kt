package ru.fredboy.cavedroid.gdx.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.gdx.CaveDroidSoundPlayer

@Module
abstract class ApplicationModule {

    @Binds
    internal abstract fun bindSoundPlayer(impl: CaveDroidSoundPlayer): SoundPlayer
}
