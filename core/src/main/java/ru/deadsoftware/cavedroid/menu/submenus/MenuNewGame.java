package ru.deadsoftware.cavedroid.menu.submenus;

import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.menu.MenuProc;
import ru.deadsoftware.cavedroid.menu.objects.ButtonEventListener;
import ru.deadsoftware.cavedroid.menu.objects.ButtonRenderer;
import ru.deadsoftware.cavedroid.misc.utils.AssetLoader;

import javax.inject.Inject;
import java.util.HashMap;

public class MenuNewGame extends Menu {

    public MenuNewGame(float width,
                       float height,
                       ButtonRenderer buttonRenderer,
                       MainConfig mainConfig,
                       MenuProc.Input menuInput,
                       AssetLoader assetLoader) {
        super(width, height, buttonRenderer, mainConfig, menuInput, assetLoader);
    }

    @Override
    protected HashMap<String, ButtonEventListener> getButtonEventListeners() {
        HashMap<String, ButtonEventListener> map = new HashMap<>();
        map.put("survival", mMenuInput::survivalClicked);
        map.put("creative", mMenuInput::creativeClicked);
        map.put("back", mMenuInput::backClicked);
        return map;
    }

    @Override
    protected void initButtons() {
        loadButtonsFromJson(mAssetLoader.getAssetHandle("json/menu_new_game_buttons.json"));
    }

    public static class Factory {

        private final MainConfig mMainConfig;
        private final AssetLoader mAssetLoader;

        @Inject
        public Factory(MainConfig mainConfig, AssetLoader assetLoader) {
            mMainConfig = mainConfig;
            mAssetLoader = assetLoader;
        }

        public MenuNewGame get(float width, float height, ButtonRenderer buttonRenderer, MenuProc.Input menuInput) {
            return new MenuNewGame(width, height, buttonRenderer, mMainConfig, menuInput, mAssetLoader);
        }

    }
}
