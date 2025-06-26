package ru.deadsoftware.cavedroid.game;

import dagger.Module;
import dagger.Provides;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.fredboy.cavedroid.common.di.GameScope;
import ru.fredboy.cavedroid.domain.assets.repository.MobAssetsRepository;
import ru.fredboy.cavedroid.domain.configuration.repository.GameConfigurationRepository;
import ru.fredboy.cavedroid.domain.items.model.block.Block;
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository;
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase;
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase;
import ru.fredboy.cavedroid.domain.save.model.GameMapSaveData;
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository;
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory;
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerWorldAdapter;
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter;
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter;
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter;
import ru.fredboy.cavedroid.game.controller.container.ContainerController;
import ru.fredboy.cavedroid.game.controller.drop.DropController;
import ru.fredboy.cavedroid.game.controller.mob.MobController;
import ru.fredboy.cavedroid.game.world.GameWorld;

@Module
public class GameModule {

    private static boolean needLoad = false;

    public static void setNeedLoad(final boolean needLoad) {
        GameModule.needLoad = needLoad;
    }

    @Provides
    @GameScope
    public static DropController provideDropController(GameConfigurationRepository gameConfigurationRepository,
                                                       SaveDataRepository saveDataRepository,
                                                       ItemsRepository itemsRepository,
                                                       DropWorldAdapter dropWorldAdapter) {

        DropController controller = needLoad
                ? saveDataRepository.loadDropController(gameConfigurationRepository.getGameDirectory(), dropWorldAdapter)
                : new DropController(itemsRepository, dropWorldAdapter);

        return controller;
    }

    @Provides
    @GameScope
    public static ContainerController provideFurnaceController(GameConfigurationRepository gameConfigurationRepository,
                                                               SaveDataRepository saveDataRepository,
                                                               GetItemByKeyUseCase getItemByKeyUseCase,
                                                               ContainerWorldAdapter containerWorldAdapter,
                                                               ContainerFactory containerFactory,
                                                               DropAdapter dropAdapter) {
        ContainerController controller = needLoad
                ? saveDataRepository.loadContainerController(gameConfigurationRepository.getGameDirectory(), containerWorldAdapter, containerFactory, dropAdapter)
                : new ContainerController(getItemByKeyUseCase, containerWorldAdapter, containerFactory, dropAdapter);

        return controller;
    }

    @Provides
    @GameScope
    public static MobController provideMobsController(GameConfigurationRepository gameConfigurationRepository,
                                                      SaveDataRepository saveDataRepository,
                                                      MobAssetsRepository mobAssetsRepository,
                                                      GetFallbackItemUseCase getFallbackItemUseCase,
                                                      MobWorldAdapter mobWorldAdapter) {

        return needLoad
                ? saveDataRepository.loadMobController(gameConfigurationRepository.getGameDirectory(), mobWorldAdapter)
                : new MobController(mobAssetsRepository, getFallbackItemUseCase, mobWorldAdapter);
    }

    @Provides
    @GameScope
    public static GameWorld provideGameWorld(GameConfigurationRepository gameConfigurationRepository,
                                             SaveDataRepository saveDataRepository,
                                             ItemsRepository itemsRepository) {

        final GameMapSaveData mapData = needLoad ? saveDataRepository.loadMap(gameConfigurationRepository.getGameDirectory()) : null;

        Block[][] fm = mapData != null ? mapData.retrieveForeMap() : null;
        Block[][] bm = mapData != null ? mapData.retrieveBackMap() : null;

        return new GameWorld(itemsRepository, fm, bm);
    }

}
