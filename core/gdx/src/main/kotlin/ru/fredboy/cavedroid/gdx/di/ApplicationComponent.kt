package ru.fredboy.cavedroid.gdx.di

import dagger.BindsInstance
import dagger.Component
import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.api.PreferencesStore
import ru.fredboy.cavedroid.data.assets.di.DataAssetsModule
import ru.fredboy.cavedroid.data.configuration.di.ApplicationContextModule
import ru.fredboy.cavedroid.data.configuration.model.ApplicationContext
import ru.fredboy.cavedroid.data.items.di.DataItemsModule
import ru.fredboy.cavedroid.data.save.di.DataSaveModule
import ru.fredboy.cavedroid.domain.assets.repository.BlockAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.BlockDamageAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.EnvironmentTextureRegionsRepository
import ru.fredboy.cavedroid.domain.assets.repository.FontAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.ItemsAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.MobAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.TextureRegionsAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.TouchButtonsAssetsRepository
import ru.fredboy.cavedroid.domain.assets.usecase.DisposeAssetsUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.InitializeAssetsUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.gdx.game.GameScreen
import ru.fredboy.cavedroid.gdx.menu.v2.MenuScreen
import ru.fredboy.cavedroid.gdx.menu.v2.PauseMenuScreen
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        DataAssetsModule::class,
        DataItemsModule::class,
        DataSaveModule::class,
        ApplicationContextModule::class,
    ],
)
interface ApplicationComponent {

    val initializeAssets: InitializeAssetsUseCase

    val disposeAssets: DisposeAssetsUseCase

    val gameScreen: GameScreen

    val applicationContextRepository: ApplicationContextRepository

    val blockAssetsRepository: BlockAssetsRepository

    val blockDamageAssetsRepository: BlockDamageAssetsRepository

    val fontAssetsRepository: FontAssetsRepository

    val mobAssetsRepository: MobAssetsRepository

    val itemAssetsRepository: ItemsAssetsRepository

    val textureRegionsAssetsRepository: TextureRegionsAssetsRepository

    val touchButtonsAssetsRepository: TouchButtonsAssetsRepository

    val itemsRepository: ItemsRepository

    val saveDataRepository: SaveDataRepository

    val applicationController: ApplicationController

    val mobParamsRepository: MobParamsRepository

    val environmentTextureRegionsRepository: EnvironmentTextureRegionsRepository

    val menuScreen: MenuScreen

    val pauseMenuScreen: PauseMenuScreen

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun applicationContext(context: ApplicationContext): Builder

        @BindsInstance
        fun applicationController(impl: ApplicationController): Builder

        @BindsInstance
        fun preferencesStore(impl: PreferencesStore): Builder

        fun build(): ApplicationComponent
    }
}
