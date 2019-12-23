package ru.deadsoftware.cavedroid.menu.submenus;

import com.badlogic.gdx.Gdx;
import ru.deadsoftware.cavedroid.game.GameSaver;
import ru.deadsoftware.cavedroid.menu.objects.Button;
import ru.deadsoftware.cavedroid.menu.objects.ButtonEventListener;
import ru.deadsoftware.cavedroid.menu.objects.ButtonRenderer;

import java.util.HashMap;

public class MenuMain extends Menu {

    public MenuMain(float width, float height, ButtonRenderer buttonRenderer) {
        super(width, height, buttonRenderer);
    }

    @Override
    protected HashMap<String, ButtonEventListener> getButtonEventListeners() {
        HashMap<String, ButtonEventListener> map = new HashMap<>();
        map.put("new_game", MenuInput::newGameClicked);
        map.put("load_game", MenuInput::loadGameClicked);
        map.put("quit", MenuInput::quitClicked);
        return map;
    }

    @Override
    protected void initButtons() {
        loadButtonsFromJson(Gdx.files.internal("json/menu_main_buttons.json"));
        if (GameSaver.exists()) {
            getButtons().get("load_game").setType(Button.NORMAL);
        }
    }
}
