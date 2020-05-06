package ru.deadsoftware.cavedroid.game;

import dagger.Module;
import dagger.Provides;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.objects.DropController;

import javax.annotation.CheckForNull;

@Module
public class GameModule {

    @CheckForNull
    private static GameSaver.Data data;

    public static void load(MainConfig mainConfig) {
        data = GameSaver.load(mainConfig);
    }

    private static void makeDataNullIfEmpty() {
        if (data != null && data.isEmpty()) {
            data = null;
        }
    }

    @Provides
    @GameScope
    public static DropController provideDropController() {
        DropController controller = data != null ? data.retrieveDropController() : new DropController();
        makeDataNullIfEmpty();
        return controller;
    }

    @Provides
    @GameScope
    public static MobsController provideMobsController() {
        MobsController controller = data != null ? data.retrieveMobsController() : new MobsController();
        makeDataNullIfEmpty();
        return controller;
    }

    @Provides
    @GameScope
    public static GameWorld provideGameWorld(DropController dropController, MobsController mobsController) {
        int[][] fm = data != null ? data.retrieveForeMap() : null;
        int[][] bm = data != null ? data.retrieveBackMap() : null;
        makeDataNullIfEmpty();
        return new GameWorld(dropController, mobsController, fm, bm);
    }

}
