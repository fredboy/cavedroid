package ru.deadsoftware.cavedroid;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import ru.deadsoftware.cavedroid.game.GameItems;
import ru.deadsoftware.cavedroid.game.GameScreen;
import ru.deadsoftware.cavedroid.misc.Assets;

public class CaveGame extends Game {

    private static final String TAG = "CaveGame";

    public static final String VERSION = "alpha 0.4";

    private final MainConfig mMainConfig;
    private final MainComponent mMainComponent;

    private final String mGameFolder;
    private final boolean mTouch;
    private boolean mDebug;

    public CaveGame(String gameFolder, boolean touch) {
        mGameFolder = gameFolder;
        mTouch = touch;

        mMainComponent = DaggerMainComponent.builder().caveGame(this).build();
        mMainConfig = mMainComponent.getMainConfig();
    }

    public void setDebug(boolean debug) {
        mDebug = debug;
    }

    private void initConfig() {
        int width = mTouch ? 320 : 480;
        int height = (int) (width * ((float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth()));

        mMainConfig.setMainComponent(mMainComponent);
        mMainConfig.setGameFolder(mGameFolder);
        mMainConfig.setTouch(mTouch);
        mMainConfig.setWidth(width);
        mMainConfig.setHeight(height);
        mMainConfig.setShowInfo(mDebug);
    }

    public void newGame() {
        GameScreen gameScreen = mMainComponent.getGameScreen();
        gameScreen.newGame();
        setScreen(gameScreen);
    }

    public void loadGame() {
        GameScreen gameScreen = mMainComponent.getGameScreen();
        gameScreen.loadGame();
        setScreen(gameScreen);
    }

    public void quitGame() {
        setScreen(mMainComponent.getMenuScreen());
    }

    @Override
    public void create() {
        Gdx.app.log(TAG, mGameFolder);
        Gdx.files.absolute(mGameFolder).mkdirs();

        Assets.load();
        GameItems.load();

        initConfig();

        setScreen(mMainComponent.getMenuScreen());
    }

}
