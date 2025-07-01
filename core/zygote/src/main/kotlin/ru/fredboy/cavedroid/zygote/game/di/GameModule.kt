package ru.fredboy.cavedroid.zygote.game.di

import dagger.Module
import dagger.Provides
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.repository.MobAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerWorldAdapter
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld

@Module
object GameModule {

    @Provides
    @GameScope
    fun provideDropController(
        applicationContextRepository: ApplicationContextRepository,
        gameContextRepository: GameContextRepository,
        saveDataRepository: SaveDataRepository,
        itemsRepository: ItemsRepository,
        dropWorldAdapter: DropWorldAdapter,
    ): DropController {
        return if (gameContextRepository.isLoadGame()) {
            saveDataRepository.loadDropController(
                gameDataFolder = applicationContextRepository.getGameDirectory(),
                dropWorldAdapter = dropWorldAdapter,
            )
        } else {
            DropController(
                itemsRepository = itemsRepository,
                dropWorldAdapter = dropWorldAdapter,
            )
        }
    }

    @Provides
    @GameScope
    fun provideContainerController(
        applicationContextRepository: ApplicationContextRepository,
        gameContextRepository: GameContextRepository,
        saveDataRepository: SaveDataRepository,
        getItemByKeyUseCase: GetItemByKeyUseCase,
        containerWorldAdapter: ContainerWorldAdapter,
        containerFactory: ContainerFactory,
        dropAdapter: DropAdapter,
    ): ContainerController {
        return if (gameContextRepository.isLoadGame()) {
            saveDataRepository.loadContainerController(
                gameDataFolder = applicationContextRepository.getGameDirectory(),
                containerWorldAdapter = containerWorldAdapter,
                containerFactory = containerFactory,
                dropAdapter = dropAdapter,
            )
        } else {
            ContainerController(
                getItemByKeyUseCase = getItemByKeyUseCase,
                containerWorldAdapter = containerWorldAdapter,
                containerFactory = containerFactory,
                dropAdapter = dropAdapter,
            )
        }
    }

    @Provides
    @GameScope
    fun provideMobController(
        applicationContextRepository: ApplicationContextRepository,
        gameContextRepository: GameContextRepository,
        saveDataRepository: SaveDataRepository,
        mobAssetsRepository: MobAssetsRepository,
        getFallbackItemUseCase: GetFallbackItemUseCase,
        mobWorldAdapter: MobWorldAdapter,
    ): MobController {
        return if (gameContextRepository.isLoadGame()) {
            saveDataRepository.loadMobController(
                gameDataFolder = applicationContextRepository.getGameDirectory(),
                mobWorldAdapter = mobWorldAdapter,
            )
        } else {
            MobController(
                mobAssetsRepository = mobAssetsRepository,
                getFallbackItemUseCase = getFallbackItemUseCase,
                mobWorldAdapter = mobWorldAdapter,
            )
        }
    }

    @Provides
    @GameScope
    fun provideGameWorld(
        applicationContextRepository: ApplicationContextRepository,
        gameContextRepository: GameContextRepository,
        saveDataRepository: SaveDataRepository,
        itemsRepository: ItemsRepository,
    ): GameWorld {
        val mapData = if (gameContextRepository.isLoadGame()) {
            saveDataRepository.loadMap(
                gameDataFolder = applicationContextRepository.getGameDirectory(),
            )
        } else {
            null
        }

        return GameWorld(
            itemsRepository = itemsRepository,
            initialForeMap = mapData?.retrieveForeMap(),
            initialBackMap = mapData?.retrieveBackMap(),
        )
    }
}
