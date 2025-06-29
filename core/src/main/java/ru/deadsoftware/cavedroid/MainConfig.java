package ru.deadsoftware.cavedroid;

import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MainConfig {

    @Nullable
    private MainComponent mMainComponent;

    @Inject
    public MainConfig() {
    }

    public MainComponent getMainComponent() {
        assert mMainComponent != null;
        return mMainComponent;
    }

    public void setMainComponent(MainComponent mainComponent) {
        mMainComponent = mainComponent;
    }
}
