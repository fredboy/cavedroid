package ru.fredboy.cavedroid.gdx.di

import dagger.BindsInstance
import dagger.Component
import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.api.PreferencesStore
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.utils.TooltipManager
import ru.fredboy.cavedroid.data.assets.di.DataAssetsModule
import ru.fredboy.cavedroid.data.configuration.di.ApplicationContextModule
import ru.fredboy.cavedroid.data.configuration.model.ApplicationContext
import ru.fredboy.cavedroid.data.items.di.DataItemsModule
import ru.fredboy.cavedroid.data.save.di.DataSaveModule
import ru.fredboy.cavedroid.domain.assets.repository.BlockDamageTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.BlockTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.EnvironmentTextureRegionsRepositoryTexture
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.ItemsTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.MobTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.StepsSoundAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.TextureRegionsTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.TouchButtonsTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.UiSoundAssetsRepository
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
        ApplicationModule::class,
    ],
)
interface ApplicationComponent {

    val initializeAssets: InitializeAssetsUseCase

    val disposeAssets: DisposeAssetsUseCase

    val gameScreen: GameScreen

    val applicationContextRepository: ApplicationContextRepository

    val blockAssetsRepository: BlockTextureAssetsRepository

    val blockDamageAssetsRepository: BlockDamageTextureAssetsRepository

    val fontAssetsRepository: FontTextureAssetsRepository

    val mobAssetsRepository: MobTextureAssetsRepository

    val itemAssetsRepository: ItemsTextureAssetsRepository

    val textureRegionsAssetsRepository: TextureRegionsTextureAssetsRepository

    val touchButtonsAssetsRepository: TouchButtonsTextureAssetsRepository

    val itemsRepository: ItemsRepository

    val saveDataRepository: SaveDataRepository

    val applicationController: ApplicationController

    val mobParamsRepository: MobParamsRepository

    val environmentTextureRegionsRepository: EnvironmentTextureRegionsRepositoryTexture

    val menuScreen: MenuScreen

    val pauseMenuScreen: PauseMenuScreen

    val tooltipManager: TooltipManager

    val stepsSoundAssetsRepository: StepsSoundAssetsRepository

    val soundPlayer: SoundPlayer

    val uiSoundAssetsRepository: UiSoundAssetsRepository

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
