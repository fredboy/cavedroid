package ru.fredboy.cavedroid.data.assets.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.data.assets.internal.BlockDamageTextureAssetsRepositoryImpl
import ru.fredboy.cavedroid.data.assets.internal.BlockTextureAssetsRepositoryImpl
import ru.fredboy.cavedroid.data.assets.internal.DropSoundAssetsRepositoryImpl
import ru.fredboy.cavedroid.data.assets.internal.EnvironmentTextureRegionsRepositoryImplTexture
import ru.fredboy.cavedroid.data.assets.internal.FontTextureAssetsRepositoryImpl
import ru.fredboy.cavedroid.data.assets.internal.ItemsTextureAssetsRepositoryImpl
import ru.fredboy.cavedroid.data.assets.internal.MobSoundAssetsRepositoryImpl
import ru.fredboy.cavedroid.data.assets.internal.MobTextureAssetsRepositoryImpl
import ru.fredboy.cavedroid.data.assets.internal.StepSoundsAssetsRepositoryImpl
import ru.fredboy.cavedroid.data.assets.internal.TextureRegionsTextureAssetsRepositoryImpl
import ru.fredboy.cavedroid.data.assets.internal.TouchButtonsRepositoryImplTexture
import ru.fredboy.cavedroid.data.assets.internal.UiSoundAssetsRepositoryImpl
import ru.fredboy.cavedroid.domain.assets.repository.BlockDamageTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.BlockTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.DropSoundAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.EnvironmentTextureRegionsRepositoryTexture
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.ItemsTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.MobSoundAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.MobTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.StepsSoundAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.TextureRegionsTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.TouchButtonsTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.UiSoundAssetsRepository

@Module
abstract class DataAssetsModule {

    @Binds
    internal abstract fun bindBlockDamageAssetsRepository(
        impl: BlockDamageTextureAssetsRepositoryImpl,
    ): BlockDamageTextureAssetsRepository

    @Binds
    internal abstract fun bindMobAssetsRepository(
        impl: MobTextureAssetsRepositoryImpl,
    ): MobTextureAssetsRepository

    @Binds
    internal abstract fun bindTextureRegionsAssetsRepository(
        impl: TextureRegionsTextureAssetsRepositoryImpl,
    ): TextureRegionsTextureAssetsRepository

    @Binds
    internal abstract fun bindFontAssetsRepository(
        impl: FontTextureAssetsRepositoryImpl,
    ): FontTextureAssetsRepository

    @Binds
    internal abstract fun bindBlockAssetsRepository(
        impl: BlockTextureAssetsRepositoryImpl,
    ): BlockTextureAssetsRepository

    @Binds
    internal abstract fun bindItemsAssetsRepository(
        impl: ItemsTextureAssetsRepositoryImpl,
    ): ItemsTextureAssetsRepository

    @Binds
    internal abstract fun bindTouchButtonsAssetsRepository(
        impl: TouchButtonsRepositoryImplTexture,
    ): TouchButtonsTextureAssetsRepository

    @Binds
    internal abstract fun bindEnvironmentTextureRegionsAssetsRepository(
        impl: EnvironmentTextureRegionsRepositoryImplTexture,
    ): EnvironmentTextureRegionsRepositoryTexture

    @Binds
    internal abstract fun bindStepsSoundAssetsRepository(
        impl: StepSoundsAssetsRepositoryImpl,
    ): StepsSoundAssetsRepository

    @Binds
    internal abstract fun bindUiSoundAssetsRepository(
        impl: UiSoundAssetsRepositoryImpl,
    ): UiSoundAssetsRepository

    @Binds
    internal abstract fun bindMobSoundAssetsRepository(
        impl: MobSoundAssetsRepositoryImpl,
    ): MobSoundAssetsRepository

    @Binds
    internal abstract fun bindDropSoundAssetsRepository(
        impl: DropSoundAssetsRepositoryImpl,
    ): DropSoundAssetsRepository
}
