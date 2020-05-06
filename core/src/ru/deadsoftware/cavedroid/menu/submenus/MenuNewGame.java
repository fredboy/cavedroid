package ru.deadsoftware.cavedroid.menu.submenus;

import com.badlogic.gdx.Gdx;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.menu.MenuProc;
import ru.deadsoftware.cavedroid.menu.objects.ButtonEventListener;
import ru.deadsoftware.cavedroid.menu.objects.ButtonRenderer;

import java.util.HashMap;

public class MenuNewGame extends Menu {

    public MenuNewGame(float width, float height, ButtonRenderer buttonRenderer, MainConfig mainConfig, MenuProc.Input menuInput) {
        super(width, height, buttonRenderer, mainConfig, menuInput);
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
        loadButtonsFromJson(Gdx.files.internal("json/menu_new_game_buttons.json"));
    }
}
