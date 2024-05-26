package ru.fredboy.cavedroid.data.assets.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.data.assets.internal.*
import ru.fredboy.cavedroid.data.assets.internal.BlockDamageAssetsRepositoryImpl
import ru.fredboy.cavedroid.data.assets.internal.FontAssetsRepositoryImpl
import ru.fredboy.cavedroid.data.assets.internal.MobAssetsRepositoryImpl
import ru.fredboy.cavedroid.data.assets.internal.TextureRegionsAssetsRepositoryImpl
import ru.fredboy.cavedroid.domain.assets.repository.*

@Module
abstract class DataAssetsModule {

    @Binds
    internal abstract fun bindBlockDamageAssetsRepository(
        impl: BlockDamageAssetsRepositoryImpl
    ): BlockDamageAssetsRepository

    @Binds
    internal abstract fun bindMobAssetsRepository(
        impl: MobAssetsRepositoryImpl
    ): MobAssetsRepository

    @Binds
    internal abstract fun bindTextureRegionsAssetsRepository(
        impl: TextureRegionsAssetsRepositoryImpl
    ): TextureRegionsAssetsRepository

    @Binds
    internal abstract fun bindFontAssetsRepository(
        impl: FontAssetsRepositoryImpl
    ): FontAssetsRepository

    @Binds
    internal abstract fun bindBlockAssetsRepository(
        impl: BlockAssetsRepositoryImpl
    ): BlockAssetsRepository

    @Binds
    internal abstract fun bindItemsAssetsRepository(
        impl: ItemsAssetsRepositoryImpl
    ): ItemsAssetsRepository

    @Binds
    internal abstract fun bindTouchButtonsAssetsRepository(
        impl: TouchButtonsRepositoryImpl
    ): TouchButtonsAssetsRepository

}