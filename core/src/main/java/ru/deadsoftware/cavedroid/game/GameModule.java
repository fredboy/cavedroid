package ru.deadsoftware.cavedroid.game;

import dagger.Module;
import dagger.Provides;
import org.jetbrains.annotations.Nullable;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.ui.TooltipManager;
import ru.fredboy.cavedroid.common.di.GameScope;
import ru.fredboy.cavedroid.domain.assets.repository.MobAssetsRepository;
import ru.fredboy.cavedroid.domain.assets.usecase.GetPigSpritesUseCase;
import ru.fredboy.cavedroid.domain.assets.usecase.GetPlayerSpritesUseCase;
import ru.fredboy.cavedroid.domain.items.model.block.Block;
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository;
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase;
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase;
import ru.fredboy.cavedroid.domain.save.model.GameSaveData;
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository;
import ru.fredboy.cavedroid.game.controller.container.ContainerController;
import ru.fredboy.cavedroid.game.controller.container.impl.ContainerControllerImpl;
import ru.fredboy.cavedroid.game.controller.drop.DropController;
import ru.fredboy.cavedroid.game.controller.drop.impl.DropControllerImpl;
import ru.fredboy.cavedroid.game.controller.mob.MobController;
import ru.fredboy.cavedroid.game.controller.mob.impl.MobControllerImpl;
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
                                                       SaveDataRepository saveDataRepository) {
        load(mainConfig, saveDataRepository);
        DropController controller = data != null ? data.retrieveDropController() : new DropControllerImpl();
        makeDataNullIfEmpty();
        return controller;
    }

    @Provides
    @GameScope
    public static ContainerController provideFurnaceController(MainConfig mainConfig,
                                                               SaveDataRepository saveDataRepository,
                                                               GetItemByKeyUseCase getItemByKeyUseCase
                                                               ) {
        load(mainConfig, saveDataRepository);
        ContainerController controller = data != null
                ? data.retrieveContainerController()
                : new ContainerControllerImpl(getItemByKeyUseCase);
        makeDataNullIfEmpty();
        return controller;
    }

    @Provides
    @GameScope
    public static MobController provideMobsController(MainConfig mainConfig,
                                                      SaveDataRepository saveDataRepository,
                                                      MobAssetsRepository mobAssetsRepository,
                                                      GetFallbackItemUseCase getFallbackItemUseCase) {
        load(mainConfig, saveDataRepository);
        MobController controller = data != null
                ? data.retrieveMobsController()
                : new MobControllerImpl(mobAssetsRepository, getFallbackItemUseCase);
        makeDataNullIfEmpty();
        return controller;
    }

    @Provides
    @GameScope
    public static GameWorld provideGameWorld(MainConfig mainConfig,
                                             SaveDataRepository saveDataRepository,
                                             ItemsRepository itemsRepository,
                                             ContainerController containerController,
                                             MobController mobController,
                                             DropController dropController
                                             ) {
        load(mainConfig, saveDataRepository);
        Block[][] fm = data != null ? data.retrieveForeMap() : null;
        Block[][] bm = data != null ? data.retrieveBackMap() : null;
        makeDataNullIfEmpty();
        return new GameWorld(itemsRepository, containerController, mobController, dropController, fm, bm);
    }

}
