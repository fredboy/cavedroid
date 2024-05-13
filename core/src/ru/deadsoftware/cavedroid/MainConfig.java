package ru.deadsoftware.cavedroid;

import ru.deadsoftware.cavedroid.game.GameUiWindow;
import ru.deadsoftware.cavedroid.game.input.Joystick;
import ru.deadsoftware.cavedroid.prefs.PreferencesStore;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;

@Singleton
public class MainConfig {

    private final HashMap<String, String> mPreferencesCache = new HashMap<>();

    @CheckForNull
    private FullscreenToggleListener mFullscreenToggleListener = null;

    private final CaveGame mCaveGame;
    private final PreferencesStore mPreferencesStore;

    @CheckForNull
    private MainComponent mMainComponent;

    @CheckForNull
    private Joystick mJoystick;

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
    public MainConfig(CaveGame caveGame, PreferencesStore preferencesStore) {
        mCaveGame = caveGame;
        mPreferencesStore = preferencesStore;

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

    @CheckForNull
    public Joystick getJoystick() {
        return mJoystick;
    }

    public void setJoystick(@CheckForNull Joystick joystick) {
        mJoystick = joystick;
    }

    @CheckForNull
    public String getPreference(String key) {
        if (mPreferencesCache.containsKey(key)) {
            return mPreferencesCache.get(key);
        }

        String value = mPreferencesStore.getPreference(key);
        mPreferencesCache.put(key, value);

        return value;
    }

    public void setPreference(String key, String value) {
        mPreferencesCache.put(key, value);
        mPreferencesStore.setPreference(key, value);

        if (mFullscreenToggleListener != null && key.equals("fullscreen")) {
            mFullscreenToggleListener.onFullscreenToggled(Boolean.parseBoolean(value));
        }
    }

    public void setFullscreenToggleListener(@Nullable FullscreenToggleListener fullscreenToggleListener) {
        mFullscreenToggleListener = fullscreenToggleListener;
    }

    public boolean isUseDynamicCamera() {
        return Boolean.parseBoolean(getPreference("dyncam"));
    }

    public interface FullscreenToggleListener {
        void onFullscreenToggled(boolean value);
    }
}
