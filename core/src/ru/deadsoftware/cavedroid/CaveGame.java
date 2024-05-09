package ru.deadsoftware.cavedroid;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import ru.deadsoftware.cavedroid.game.GameScreen;
import ru.deadsoftware.cavedroid.misc.Assets;
import ru.deadsoftware.cavedroid.misc.utils.AssetLoader;

import javax.annotation.Nullable;

public class CaveGame extends Game {

    private static final String TAG = "CaveGame";

    public static final String VERSION = "alpha 0.7.1";

    private final MainConfig mMainConfig;
    private final MainComponent mMainComponent;
    private final AssetLoader mAssetLoader;

    private final String mGameFolder;
    private final boolean mTouch;
    private boolean mDebug;

    @Nullable
    private final String mAssetsPackPath;

    public CaveGame(String gameFolder, boolean touch, @Nullable String assetsPackPath) {
        mGameFolder = gameFolder;
        mTouch = touch;
        mAssetsPackPath = assetsPackPath;

        mMainComponent = DaggerMainComponent.builder().caveGame(this).build();

        mMainConfig = mMainComponent.getMainConfig();
        mAssetLoader = mMainComponent.getAssetLoader();
    }

    public void setDebug(boolean debug) {
        mDebug = debug;
    }

    private void initConfig() {
        int width = 480;
        int height = (int) (width * ((float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth()));

        mMainConfig.setMainComponent(mMainComponent);
        mMainConfig.setGameFolder(mGameFolder);
        mMainConfig.setTouch(mTouch);
        mMainConfig.setWidth(width);
        mMainConfig.setHeight(height);
        mMainConfig.setShowInfo(mDebug);
        mMainConfig.setAssetsPackPath(mAssetsPackPath);

        if (mDebug) {
            Gdx.app.setLogLevel(Application.LOG_DEBUG);
        } else {
            Gdx.app.setLogLevel(Application.LOG_ERROR);
        }
    }

    public void newGame(int gameMode) {
        GameScreen gameScreen = mMainComponent.getGameScreen();
        gameScreen.newGame(gameMode);
        setScreen(gameScreen);
    }

    public void loadGame() {
        GameScreen gameScreen = mMainComponent.getGameScreen();
        gameScreen.loadGame();
        setScreen(gameScreen);
    }

    public void quitGame() {
        if (screen != null) {
            screen.dispose();
        }
        setScreen(mMainComponent.getMenuScreen());
    }

    @Override
    public void create() {
        Gdx.files.absolute(mGameFolder).mkdirs();
        initConfig();

        Gdx.app.debug(TAG, mGameFolder);
        Assets.load(mAssetLoader);
        setScreen(mMainComponent.getMenuScreen());
    }

    @Override
    public void dispose() {
        if (screen != null) {
            screen.dispose();
        }
        Assets.dispose();
    }
}
