package ru.deadsoftware.cavedroid.menu.submenus;

import com.badlogic.gdx.Gdx;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.GameSaver;
import ru.deadsoftware.cavedroid.menu.MenuProc;
import ru.deadsoftware.cavedroid.menu.objects.Button;
import ru.deadsoftware.cavedroid.menu.objects.ButtonEventListener;
import ru.deadsoftware.cavedroid.menu.objects.ButtonRenderer;
import ru.deadsoftware.cavedroid.misc.utils.AssetLoader;

import javax.inject.Inject;
import java.util.HashMap;

public class MenuMain extends Menu {

    public MenuMain(float width,
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
        map.put("new_game", mMenuInput::newGameClicked);
        map.put("load_game", mMenuInput::loadGameClicked);
        map.put("options", mMenuInput::optionsClicked);
        map.put("quit", mMenuInput::quitClicked);
        return map;
    }

    @Override
    protected void initButtons() {
        loadButtonsFromJson(mAssetLoader.getAssetHandle("json/menu_main_buttons.json"));
        if (GameSaver.exists(mMainConfig)) {
            getButtons().get("load_game").setType(Button.NORMAL);
        }
    }

    public static class Factory {

        private final MainConfig mMainConfig;
        private final AssetLoader mAssetLoader;

        @Inject
        public Factory(MainConfig mainConfig, AssetLoader assetLoader) {
            mMainConfig = mainConfig;
            mAssetLoader = assetLoader;
        }

        public MenuMain get(float width, float height, ButtonRenderer buttonRenderer, MenuProc.Input menuInput) {
            return new MenuMain(width, height, buttonRenderer, mMainConfig, menuInput, mAssetLoader);
        }

    }

}
