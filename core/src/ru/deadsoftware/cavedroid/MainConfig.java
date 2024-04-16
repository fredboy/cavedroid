package ru.deadsoftware.cavedroid;

import ru.deadsoftware.cavedroid.game.GameUiWindow;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MainConfig {

    private final CaveGame mCaveGame;

    @CheckForNull
    private MainComponent mMainComponent;

    private GameUiWindow mGameUiWindow;
    private String mGameFolder;

    private boolean mTouch;
    private boolean mShowInfo;
    private boolean mShowMap;

    private float mWidth;
    private float mHeight;

    @Nullable
    private String mAssetsPackPath = null;

    @Inject
    public MainConfig(CaveGame caveGame) {
        mCaveGame = caveGame;

        mGameUiWindow = GameUiWindow.NONE;
        mGameFolder = "";
    }

    public CaveGame getCaveGame() {
        return mCaveGame;
    }

    public MainComponent getMainComponent() {
        assert mMainComponent != null;
        return mMainComponent;
    }

    public void setMainComponent(MainComponent mainComponent) {
        mMainComponent = mainComponent;
    }

    public boolean checkGameUiWindow(GameUiWindow gameUiWindow) {
        return mGameUiWindow == gameUiWindow;
    }

    public GameUiWindow getGameUiWindow() {
        return mGameUiWindow;
    }

    public void setGameUiWindow(GameUiWindow gameUiWindow) {
        mGameUiWindow = gameUiWindow;
    }

    public String getGameFolder() {
        return mGameFolder;
    }

    public void setGameFolder(String gameFolder) {
        mGameFolder = gameFolder;
    }

    public boolean isTouch() {
        return mTouch;
    }

    public void setTouch(boolean touch) {
        mTouch = touch;
    }

    public float getWidth() {
        return mWidth;
    }

    public void setWidth(float width) {
        mWidth = width;
    }

    public float getHeight() {
        return mHeight;
    }

    public void setHeight(float height) {
        mHeight = height;
    }

    public boolean isShowInfo() {
        return mShowInfo;
    }

    public void setShowInfo(boolean showInfo) {
        mShowInfo = showInfo;
    }

    public boolean isShowMap() {
        return mShowMap;
    }

    public void setShowMap(boolean showMap) {
        mShowMap = showMap;
    }

    @Nullable
    public String getAssetsPackPath() {
        return mAssetsPackPath;
    }

    public void setAssetsPackPath(@Nullable String assetsPackPath) {
        mAssetsPackPath = assetsPackPath;
    }
}
