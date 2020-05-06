package ru.deadsoftware.cavedroid.menu.submenus;

import com.badlogic.gdx.Gdx;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.GameSaver;
import ru.deadsoftware.cavedroid.menu.MenuProc;
import ru.deadsoftware.cavedroid.menu.objects.Button;
import ru.deadsoftware.cavedroid.menu.objects.ButtonEventListener;
import ru.deadsoftware.cavedroid.menu.objects.ButtonRenderer;

import java.util.HashMap;

public class MenuMain extends Menu {

    public MenuMain(float width, float height, ButtonRenderer buttonRenderer, MainConfig mainConfig, MenuProc.Input menuInput) {
        super(width, height, buttonRenderer, mainConfig, menuInput);
    }

    @Override
    protected HashMap<String, ButtonEventListener> getButtonEventListeners() {
        HashMap<String, ButtonEventListener> map = new HashMap<>();
        map.put("new_game", mMenuInput::newGameClicked);
        map.put("load_game", mMenuInput::loadGameClicked);
        map.put("quit", mMenuInput::quitClicked);
        return map;
    }

    @Override
    protected void initButtons() {
        loadButtonsFromJson(Gdx.files.internal("json/menu_main_buttons.json"));
        if (GameSaver.exists(mMainConfig)) {
            getButtons().get("load_game").setType(Button.NORMAL);
        }
    }
}
