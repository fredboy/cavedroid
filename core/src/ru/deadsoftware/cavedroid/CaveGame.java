package ru.deadsoftware.cavedroid;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import ru.deadsoftware.cavedroid.game.GameItems;
import ru.deadsoftware.cavedroid.misc.Assets;

public class CaveGame extends Game {

    private static final String TAG = "CaveGame";

    public static final String VERSION = "alpha 0.4";

    private final String mGameFolder;
    private final boolean mTouch;
    private boolean mDebug;

    public CaveGame(String gameFolder, boolean touch) {
        mGameFolder = gameFolder;
        mTouch = touch;
    }

    public void setDebug(boolean debug) {
        mDebug = debug;
    }

    private void initConfig(MainConfig mainConfig, MainComponent mainComponent) {
        int width = mTouch ? 320 : 480;
        int height = (int) (width * ((float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth()));

        mainConfig.setMainComponent(mainComponent);
        mainConfig.setGameFolder(mGameFolder);
        mainConfig.setTouch(mTouch);
        mainConfig.setWidth(width);
        mainConfig.setHeight(height);
        mainConfig.setShowInfo(true);
    }

    @Override
    public void create() {
        Gdx.app.log(TAG, mGameFolder);
        Gdx.files.absolute(mGameFolder).mkdirs();

        Assets.load();
        GameItems.load();

        MainComponent mainComponent = DaggerMainComponent.create();
        initConfig(mainComponent.getGameConfig(), mainComponent);
        setScreen(mainComponent.getGameScreen());
    }

}
