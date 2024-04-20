package ru.deadsoftware.cavedroid.game;

import dagger.Module;
import dagger.Provides;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.model.block.Block;
import ru.deadsoftware.cavedroid.game.objects.DropController;
import ru.deadsoftware.cavedroid.game.world.GameWorld;

import javax.annotation.CheckForNull;

@Module
public class GameModule {

    @CheckForNull
    private static GameSaver.Data data;

    public static boolean loaded = false;

    private static void load(MainConfig mainConfig, GameItemsHolder gameItemsHolder) {
        if (loaded) {
            return;
        }
        data = GameSaver.load(mainConfig, gameItemsHolder);
        loaded = true;
    }

    private static void makeDataNullIfEmpty() {
        if (data != null && data.isEmpty()) {
            data = null;
        }
    }

    @Provides
    @GameScope
    public static DropController provideDropController(MainConfig mainConfig, GameItemsHolder gameItemsHolder) {
        load(mainConfig, gameItemsHolder);
        DropController controller = data != null ? data.retrieveDropController() : new DropController();
        makeDataNullIfEmpty();
        controller.initDrops(gameItemsHolder);
        return controller;
    }

    @Provides
    @GameScope
    public static MobsController provideMobsController(MainConfig mainConfig, GameItemsHolder gameItemsHolder) {
        load(mainConfig, gameItemsHolder);
        MobsController controller = data != null ? data.retrieveMobsController() : new MobsController(gameItemsHolder);
        makeDataNullIfEmpty();
        controller.getPlayer().initInventory(gameItemsHolder);
        return controller;
    }

    @Provides
    @GameScope
    public static GameWorld provideGameWorld(MainConfig mainConfig,
                                             DropController dropController,
                                             MobsController mobsController,
                                             GameItemsHolder gameItemsHolder) {
        load(mainConfig, gameItemsHolder);
        Block[][] fm = data != null ? data.retrieveForeMap() : null;
        Block[][] bm = data != null ? data.retrieveBackMap() : null;
        makeDataNullIfEmpty();
        return new GameWorld(dropController, mobsController, gameItemsHolder, fm, bm);
    }

}
