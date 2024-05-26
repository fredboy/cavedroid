package ru.deadsoftware.cavedroid.game;

import dagger.Module;
import dagger.Provides;
import org.jetbrains.annotations.Nullable;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.model.block.Block;
import ru.deadsoftware.cavedroid.game.objects.drop.DropController;
import ru.deadsoftware.cavedroid.game.objects.container.ContainerController;
import ru.deadsoftware.cavedroid.game.save.GameSaveData;
import ru.deadsoftware.cavedroid.game.save.GameSaveLoader;
import ru.deadsoftware.cavedroid.game.ui.TooltipManager;
import ru.deadsoftware.cavedroid.game.world.GameWorld;
import ru.fredboy.cavedroid.domain.assets.usecase.GetPigSpritesUseCase;
import ru.fredboy.cavedroid.domain.assets.usecase.GetPlayerSpritesUseCase;

@Module
public class GameModule {

    @Nullable
    private static GameSaveData data;

    public static boolean loaded = false;

    private static void load(MainConfig mainConfig,
                             GameItemsHolder gameItemsHolder,
                             TooltipManager tooltipManager,
                             GetPlayerSpritesUseCase getPlayerSpritesUseCase,
                             GetPigSpritesUseCase getPigSpritesUseCase) {
        if (loaded) {
            return;
        }

        data = GameSaveLoader.INSTANCE.load(mainConfig,
                gameItemsHolder,
                tooltipManager,
                getPlayerSpritesUseCase,
                getPigSpritesUseCase);

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
                                                       GameItemsHolder gameItemsHolder,
                                                       TooltipManager tooltipManager,
                                                       GetPlayerSpritesUseCase getPlayerSpritesUseCase,
                                                       GetPigSpritesUseCase getPigSpritesUseCase) {
        load(mainConfig, gameItemsHolder, tooltipManager, getPlayerSpritesUseCase, getPigSpritesUseCase);
        DropController controller = data != null ? data.retrieveDropController() : new DropController();
        makeDataNullIfEmpty();
        controller.initDrops(gameItemsHolder);
        return controller;
    }

    @Provides
    @GameScope
    public static ContainerController provideFurnaceController(MainConfig mainConfig,
                                                               DropController dropController,
                                                               GameItemsHolder gameItemsHolder,
                                                               TooltipManager tooltipManager,
                                                               GetPlayerSpritesUseCase getPlayerSpritesUseCase,
                                                               GetPigSpritesUseCase getPigSpritesUseCase) {
        load(mainConfig, gameItemsHolder, tooltipManager, getPlayerSpritesUseCase, getPigSpritesUseCase);
        ContainerController controller = data != null
                ? data.retrieveContainerController()
                : new ContainerController(dropController, gameItemsHolder);
        makeDataNullIfEmpty();
        controller.init(dropController, gameItemsHolder);
        return controller;
    }

    @Provides
    @GameScope
    public static MobsController provideMobsController(MainConfig mainConfig,
                                                       GameItemsHolder gameItemsHolder,
                                                       TooltipManager tooltipManager,
                                                       GetPlayerSpritesUseCase getPlayerSpritesUseCase,
                                                       GetPigSpritesUseCase getPigSpritesUseCase) {
        load(mainConfig, gameItemsHolder, tooltipManager, getPlayerSpritesUseCase, getPigSpritesUseCase);
        MobsController controller = data != null
                ? data.retrieveMobsController()
                : new MobsController(gameItemsHolder, tooltipManager, getPlayerSpritesUseCase);
        makeDataNullIfEmpty();
        controller.getPlayer().initInventory(gameItemsHolder, tooltipManager);
        return controller;
    }

    @Provides
    @GameScope
    public static GameWorld provideGameWorld(MainConfig mainConfig,
                                             DropController dropController,
                                             MobsController mobsController,
                                             GameItemsHolder gameItemsHolder,
                                             ContainerController containerController,
                                             TooltipManager tooltipManager,
                                             GetPlayerSpritesUseCase getPlayerSpritesUseCase,
                                             GetPigSpritesUseCase getPigSpritesUseCase) {
        load(mainConfig, gameItemsHolder, tooltipManager, getPlayerSpritesUseCase, getPigSpritesUseCase);
        Block[][] fm = data != null ? data.retrieveForeMap() : null;
        Block[][] bm = data != null ? data.retrieveBackMap() : null;
        makeDataNullIfEmpty();
        return new GameWorld(dropController, mobsController, gameItemsHolder, containerController, fm, bm);
    }

}
