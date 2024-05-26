package ru.deadsoftware.cavedroid.menu.submenus;

import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.menu.MenuProc;
import ru.deadsoftware.cavedroid.menu.objects.ButtonEventListener;
import ru.deadsoftware.cavedroid.menu.objects.ButtonRenderer;
import ru.deadsoftware.cavedroid.misc.utils.AssetLoader;
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase;

import javax.inject.Inject;
import java.util.HashMap;

public class MenuNewGame extends Menu {

    public MenuNewGame(float width,
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
        map.put("survival", mMenuInput::survivalClicked);
        map.put("creative", mMenuInput::creativeClicked);
        map.put("back", mMenuInput::backClicked);
        return map;
    }

    @Override
    protected void initButtons() {
        loadButtonsFromJson(mAssetLoader.getAssetHandle("json/menu_new_game_buttons.json"));
    }

}
