package ru.deadsoftware.cavedroid.menu.submenus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.JsonValue;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.GameScreen;
import ru.deadsoftware.cavedroid.menu.objects.Button;
import ru.deadsoftware.cavedroid.menu.objects.ButtonRenderer;
import ru.deadsoftware.cavedroid.menu.objects.ButtonEventListener;
import ru.deadsoftware.cavedroid.misc.Assets;

import java.util.HashMap;

public abstract class Menu {

    private ButtonRenderer buttonRenderer;

    private float width;
    private float height;

    /**
     * {@link ArrayMap} of {@link Button Buttons} of this menu screen
     */
    private ArrayMap<String, Button> buttons;

    /**
     * @param width  Viewport width
     * @param height Viewport height
     * @param buttonRenderer {@link ButtonRenderer} that will draw the buttons of this menu
     */
    Menu(float width, float height, ButtonRenderer buttonRenderer) {
        this.width = width;
        this.height = height;
        this.buttonRenderer = buttonRenderer;
        initButtons();
    }

    /**
     * If you are loading buttons from json,
     * override this method and create a HashMap with buttons' keys from json as keys
     * and {@link ButtonEventListener ButtonEventListeners} as values.
     *
     * @return empty HashMap if not overridden
     */
    protected HashMap<String, ButtonEventListener> getButtonEventListeners() {
        return new HashMap<>();
    }

    /**
     * You can call this from {@link #initButtons()} to load buttons from json
     *
     * @param jsonFile A {@link FileHandle} to json file
     */
    void loadButtonsFromJson(FileHandle jsonFile) {
        if (buttons == null) buttons = new ArrayMap<>();
        HashMap<String, ButtonEventListener> eventListeners = getButtonEventListeners();
        JsonValue json = Assets.jsonReader.parse(jsonFile);
        int y = (int) height / 4;
        for (JsonValue key = json.child(); key != null; key = key.next(), y += Button.HEIGHT + 10) {
            buttons.put(key.name(),
                    new Button(Assets.getStringFromJson(key, "label", ""),
                            (int) width / 2 - Button.WIDTH / 2,
                            Assets.getIntFromJson(key, "y", y), Assets.getIntFromJson(key, "type", Button.NORMAL),
                            eventListeners.containsKey(key.name()) ? eventListeners.get(key.name()) : () -> {
                            }));
        }
    }

    /**
     * Draws the menu with background, logo and it's buttons
     *
     * @param spriter {@link SpriteBatch} that will draw it. Should be already started.
     */
    public void draw(SpriteBatch spriter) {
        TextureRegion background = Assets.textureRegions.get("background");
        TextureRegion gamelogo = Assets.textureRegions.get("gamelogo");

        for (int x = 0; x <= width / 16; x++) {
            for (int y = 0; y <= height / 16; y++) {
                spriter.draw(background, x * 16, y * 16);
            }
        }
        spriter.draw(gamelogo, width / 2 - (float) gamelogo.getRegionWidth() / 2, 8);

        float inputX = Gdx.input.getX() * width / GameScreen.getWidth();
        float inputY = Gdx.input.getY() * height / GameScreen.getHeight();
        for (Button button : buttons.values()) {
            if (button.getType() > 0) {
                if (button.getRect().contains(inputX, inputY) && (!CaveGame.TOUCH || Gdx.input.isTouched())) {
                    button.setType(2);
                } else {
                    button.setType(1);
                }
            }
            button.draw(buttonRenderer);
        }
    }

    public ArrayMap<String, Button> getButtons() {
        return buttons;
    }

    /**
     * This method is called from constructor and should initialize {@link #buttons} <br>
     * You can run {@link #loadButtonsFromJson(FileHandle)} from it
     */
    protected abstract void initButtons();
}
