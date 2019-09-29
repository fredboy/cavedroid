package ru.deadsoftware.cavedroid.menu.submenus;

import com.badlogic.gdx.Gdx;
import ru.deadsoftware.cavedroid.menu.objects.ButtonRenderer;
import ru.deadsoftware.cavedroid.menu.objects.ButtonEventListener;

import java.util.HashMap;

public class MenuNewGame extends Menu {

    public MenuNewGame(float width, float height, ButtonRenderer buttonRenderer) {
        super(width, height, buttonRenderer);
    }

    @Override
    protected HashMap<String, ButtonEventListener> getButtonEventListeners() {
        HashMap<String, ButtonEventListener> map = new HashMap<>();
        map.put("survival", MenuInput::survivalClicked);
        map.put("creative", MenuInput::creativeClicked);
        map.put("back", MenuInput::backClicked);
        return map;
    }

    @Override
    protected void initButtons() {
        loadButtonsFromJson(Gdx.files.internal("json/menu_new_game_buttons.json"));
    }
}
