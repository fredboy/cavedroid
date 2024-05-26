package ru.deadsoftware.cavedroid

import dagger.Component
import ru.deadsoftware.cavedroid.game.GameScreen
import ru.deadsoftware.cavedroid.menu.MenuScreen
import ru.deadsoftware.cavedroid.misc.utils.AssetLoader
import ru.deadsoftware.cavedroid.prefs.PreferencesStore
import ru.fredboy.cavedroid.data.assets.di.DataAssetsModule
import ru.fredboy.cavedroid.domain.assets.GameAssetsHolder
import ru.fredboy.cavedroid.domain.assets.repository.*
import ru.fredboy.cavedroid.domain.assets.usecase.DisposeAssetsUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.InitializeAssetsUseCase
import javax.inject.Singleton

@Singleton
@Component(
    dependencies = [CaveGame::class, PreferencesStore::class],
    modules = [DataAssetsModule::class]
)
interface MainComponent {

    val gameScreen: GameScreen

    val menuScreen: MenuScreen

    val mainConfig: MainConfig

    val assetLoader: AssetLoader

    val initializeAssetsUseCase: InitializeAssetsUseCase

    val disposeAssetsUseCase: DisposeAssetsUseCase

    val blockAssetsRepository: BlockAssetsRepository

    val blockDamageAssetsRepository: BlockDamageAssetsRepository

    val fontAssetsRepository: FontAssetsRepository

    val mobAssetsRepository: MobAssetsRepository

    val itemAssetsRepository: ItemsAssetsRepository

    val textureRegionsAssetsRepository: TextureRegionsAssetsRepository

    val touchButtonsAssetsRepository: TouchButtonsAssetsRepository

}