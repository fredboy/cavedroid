package ru.deadsoftware.cavedroid;

import org.jetbrains.annotations.Nullable;
import ru.deadsoftware.cavedroid.prefs.PreferencesStore;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;

@Singleton
public class MainConfig {

    private final HashMap<String, String> mPreferencesCache = new HashMap<>();

    @Nullable
    private FullscreenToggleListener mFullscreenToggleListener = null;

    private final CaveGame mCaveGame;
    private final PreferencesStore mPreferencesStore;

    @Nullable
    private MainComponent mMainComponent;

    @Nullable
    private String mAssetsPackPath = null;

    @Inject
    public MainConfig(CaveGame caveGame, PreferencesStore preferencesStore) {
        mCaveGame = caveGame;
        mPreferencesStore = preferencesStore;
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

    @Nullable
    public String getAssetsPackPath() {
        return mAssetsPackPath;
    }

    @Nullable
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
