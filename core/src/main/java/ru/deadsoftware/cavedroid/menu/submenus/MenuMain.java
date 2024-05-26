package ru.deadsoftware.cavedroid.menu.submenus;

import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.save.GameSaveLoader;
import ru.deadsoftware.cavedroid.menu.MenuProc;
import ru.deadsoftware.cavedroid.menu.objects.Button;
import ru.deadsoftware.cavedroid.menu.objects.ButtonEventListener;
import ru.deadsoftware.cavedroid.menu.objects.ButtonRenderer;
import ru.deadsoftware.cavedroid.misc.utils.AssetLoader;
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase;

import javax.inject.Inject;
import java.util.HashMap;

public class MenuMain extends Menu {

    public MenuMain(float width,
                    float height,
                    ButtonRenderer buttonRenderer,
                    MainConfig mainConfig,
                    MenuProc.Input menuInput,
                    AssetLoader assetLoader,
                    GetTextureRegionByNameUseCase getTextureRegionByNameUseCase) {
        super(width, height, buttonRenderer, mainConfig, menuInput, assetLoader, getTextureRegionByNameUseCase);
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
        if (GameSaveLoader.INSTANCE.exists(mMainConfig)) {
            getButtons().get("load_game").setType(Button.NORMAL);
        }
    }

}
