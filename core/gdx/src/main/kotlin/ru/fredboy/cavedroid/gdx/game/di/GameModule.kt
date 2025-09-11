package ru.fredboy.cavedroid.gdx.game.di

import dagger.Module
import dagger.Provides
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.TooltipManager
import ru.fredboy.cavedroid.domain.assets.repository.DropSoundAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.EnvironmentTextureRegionsRepositoryTexture
import ru.fredboy.cavedroid.domain.assets.repository.StepsSoundAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerWorldAdapter
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.ProjectileAdapter
import ru.fredboy.cavedroid.entity.projectile.abstraction.ProjectileWorldAdapter
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.MobSoundManager
import ru.fredboy.cavedroid.game.controller.projectile.ProjectileController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.GameWorldContactListener
import ru.fredboy.cavedroid.game.world.GameWorldLightManager
import ru.fredboy.cavedroid.game.world.abstraction.GameWorldSolidBlockBodiesManager

@Module
object GameModule {

    @Provides
    @GameScope
    fun provideDropController(
        applicationContextRepository: ApplicationContextRepository,
        gameContextRepository: GameContextRepository,
        saveDataRepository: SaveDataRepository,
        dropWorldAdapter: DropWorldAdapter,
        dropQueue: DropQueue,
        playerAdapter: PlayerAdapter,
        getItemByKeyUseCase: GetItemByKeyUseCase,
        dropSoundAssetsRepository: DropSoundAssetsRepository,
        soundPlayer: SoundPlayer,
    ): DropController = if (gameContextRepository.isLoadGame()) {
        saveDataRepository.loadDropController(
            gameDataFolder = applicationContextRepository.getGameDirectory(),
            saveGameDirectory = gameContextRepository.getSaveGameDirectory(),
            dropWorldAdapter = dropWorldAdapter,
            dropQueue = dropQueue,
            playerAdapter = playerAdapter,
        )
    } else {
        DropController(
            dropWorldAdapter = dropWorldAdapter,
            dropQueue = dropQueue,
            playerAdapter = playerAdapter,
            getItemByKeyUseCase = getItemByKeyUseCase,
            dropSoundAssetsRepository = dropSoundAssetsRepository,
            soundPlayer = soundPlayer,
        )
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
    ): ContainerController = if (gameContextRepository.isLoadGame()) {
        saveDataRepository.loadContainerController(
            gameDataFolder = applicationContextRepository.getGameDirectory(),
            saveGameDirectory = gameContextRepository.getSaveGameDirectory(),
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

    @Provides
    @GameScope
    fun provideMobController(
        applicationContextRepository: ApplicationContextRepository,
        gameContextRepository: GameContextRepository,
        saveDataRepository: SaveDataRepository,
        mobParamsRepository: MobParamsRepository,
        getFallbackItemUseCase: GetFallbackItemUseCase,
        mobWorldAdapter: MobWorldAdapter,
        mobPhysicsFactory: MobPhysicsFactory,
        dropQueue: DropQueue,
        getItemByKeyUseCase: GetItemByKeyUseCase,
        tooltipManager: TooltipManager,
        mobSoundManager: MobSoundManager,
        soundPlayer: SoundPlayer,
        stepsSoundAssetsRepository: StepsSoundAssetsRepository,
        projectileAdapter: ProjectileAdapter,
    ): MobController = if (gameContextRepository.isLoadGame()) {
        saveDataRepository.loadMobController(
            gameDataFolder = applicationContextRepository.getGameDirectory(),
            saveGameDirectory = gameContextRepository.getSaveGameDirectory(),
            mobWorldAdapter = mobWorldAdapter,
            mobPhysicsFactory = mobPhysicsFactory,
            dropQueue = dropQueue,
            mobSoundManager = mobSoundManager,
            projectileAdapter = projectileAdapter,
        )
    } else {
        MobController(
            mobParamsRepository = mobParamsRepository,
            getFallbackItemUseCase = getFallbackItemUseCase,
            mobWorldAdapter = mobWorldAdapter,
            mobPhysicsFactory = mobPhysicsFactory,
            dropQueue = dropQueue,
            getItemByKeyUseCase = getItemByKeyUseCase,
            tooltipManager = tooltipManager,
            mobSoundManager = mobSoundManager,
            soundPlayer = soundPlayer,
            stepsSoundAssetsRepository = stepsSoundAssetsRepository,
            projectileAdapter = projectileAdapter,
        )
    }

    @Provides
    @GameScope
    fun provideProjectileController(
        applicationContextRepository: ApplicationContextRepository,
        gameContextRepository: GameContextRepository,
        saveDataRepository: SaveDataRepository,
        projectileWorldAdapter: ProjectileWorldAdapter,
        dropQueue: DropQueue,
        getItemByKeyUseCase: GetItemByKeyUseCase,
    ): ProjectileController {
        return if (gameContextRepository.isLoadGame()) {
            saveDataRepository.loadProjectileController(
                gameDataFolder = applicationContextRepository.getGameDirectory(),
                saveGameDirectory = gameContextRepository.getSaveGameDirectory(),
                projectileWorldAdapter = projectileWorldAdapter,
                dropQueue = dropQueue,
            )
        } else {
            ProjectileController(
                getItemByKeyUseCase = getItemByKeyUseCase,
                projectileWorldAdapter = projectileWorldAdapter,
                dropQueue = dropQueue,
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
        physicsController: GameWorldContactListener,
        gameWorldSolidBlockBodiesManager: GameWorldSolidBlockBodiesManager,
        environmentTextureRegionsRepository: EnvironmentTextureRegionsRepositoryTexture,
        gameWorldLightManager: GameWorldLightManager,
    ): GameWorld {
        val mapData = if (gameContextRepository.isLoadGame()) {
            saveDataRepository.loadMap(
                gameDataFolder = applicationContextRepository.getGameDirectory(),
                saveGameDirectory = gameContextRepository.getSaveGameDirectory(),
            )
        } else {
            null
        }

        return GameWorld(
            itemsRepository = itemsRepository,
            physicsController = physicsController,
            gameWorldSolidBlockBodiesManager = gameWorldSolidBlockBodiesManager,
            environmentTextureRegionsRepository = environmentTextureRegionsRepository,
            gameWorldLightManager = gameWorldLightManager,
            initialForeMap = mapData?.foreMap,
            initialBackMap = mapData?.backMap,
        ).apply {
            mapData?.let {
                this.currentGameTime = mapData.gameTime
                this.moonPhase = mapData.moonPhase
                this.totalGameTimeSec = mapData.totalGameTime
                this.lastSpawnGameTime = mapData.lastSpawnGameTime
            }
        }
    }
}
