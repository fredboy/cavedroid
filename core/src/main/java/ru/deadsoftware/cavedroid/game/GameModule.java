package ru.deadsoftware.cavedroid.game;

import dagger.Module;
import dagger.Provides;
import org.jetbrains.annotations.Nullable;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.fredboy.cavedroid.common.di.GameScope;
import ru.fredboy.cavedroid.domain.assets.repository.MobAssetsRepository;
import ru.fredboy.cavedroid.domain.items.model.block.Block;
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository;
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase;
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase;
import ru.fredboy.cavedroid.domain.save.model.GameSaveData;
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository;
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory;
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerWorldAdapter;
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter;
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter;
import ru.fredboy.cavedroid.game.controller.container.ContainerController;
import ru.fredboy.cavedroid.game.controller.drop.DropController;
import ru.fredboy.cavedroid.game.controller.mob.MobController;
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter;
import ru.fredboy.cavedroid.game.world.GameWorld;

@Module
public class GameModule {

    @Nullable
    private static GameSaveData data;

    public static boolean loaded = false;

    private static void load(MainConfig mainConfig, SaveDataRepository saveDataRepository) {
        if (loaded) {
            return;
        }

        data = saveDataRepository.load(mainConfig.getGameFolder());

        loaded = true;
    }

    private static void makeDataNullIfEmpty() {
        if (data != null && data.isEmpty()) {
            data = null;
        }
    }

    @Provides
    @GameScope
    public static DropController provideDropController(MainConfig mainConfig,
                                                       SaveDataRepository saveDataRepository,
                                                       DropWorldAdapter dropWorldAdapter,
                                                       ItemsRepository itemsRepository) {
        load(mainConfig, saveDataRepository);
        DropController controller = data != null
                ? data.retrieveDropController()
                : new DropController(itemsRepository, dropWorldAdapter);
        makeDataNullIfEmpty();
        return controller;
    }

    @Provides
    @GameScope
    public static ContainerController provideFurnaceController(MainConfig mainConfig,
                                                               SaveDataRepository saveDataRepository,
                                                               GetItemByKeyUseCase getItemByKeyUseCase,
                                                               ContainerWorldAdapter containerWorldAdapter,
                                                               ContainerFactory containerFactory,
                                                               DropAdapter dropAdapter
                                                               ) {
        load(mainConfig, saveDataRepository);
        ContainerController controller = data != null
                ? data.retrieveContainerController()
                : new ContainerController(getItemByKeyUseCase, containerWorldAdapter, containerFactory, dropAdapter);
        makeDataNullIfEmpty();
        return controller;
    }

    @Provides
    @GameScope
    public static MobController provideMobsController(MainConfig mainConfig,
                                                      SaveDataRepository saveDataRepository,
                                                      MobAssetsRepository mobAssetsRepository,
                                                      GetFallbackItemUseCase getFallbackItemUseCase,
                                                      MobWorldAdapter mobWorldAdapter
    ) {
        load(mainConfig, saveDataRepository);
        MobController controller = data != null
                ? data.retrieveMobsController()
                : new MobController(mobAssetsRepository, getFallbackItemUseCase, mobWorldAdapter);
        makeDataNullIfEmpty();
        return controller;
    }

    @Provides
    @GameScope
    public static GameWorld provideGameWorld(MainConfig mainConfig,
                                             SaveDataRepository saveDataRepository,
                                             ItemsRepository itemsRepository
                                             ) {
        load(mainConfig, saveDataRepository);
        Block[][] fm = data != null ? data.retrieveForeMap() : null;
        Block[][] bm = data != null ? data.retrieveBackMap() : null;
        makeDataNullIfEmpty();
        return new GameWorld(itemsRepository, fm, bm);
    }

}
