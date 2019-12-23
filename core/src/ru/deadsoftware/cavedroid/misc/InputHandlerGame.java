package ru.deadsoftware.cavedroid.misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.GameScreen;
import ru.deadsoftware.cavedroid.game.objects.TouchButton;

import static com.badlogic.gdx.utils.ObjectMap.Entry;
import static ru.deadsoftware.cavedroid.GameScreen.GP;

public class InputHandlerGame extends InputAdapter {

    private static TouchButton nullButton = new TouchButton(null, -1, true);

    public InputHandlerGame() {
        loadTouchButtonsFromJSON();
    }

    private int getMouseKey(String name) {
        switch (name) {
            case "Left":
                return Input.Buttons.LEFT;
            case "Right":
                return Input.Buttons.RIGHT;
            case "Middle":
                return Input.Buttons.MIDDLE;
            case "Back":
                return Input.Buttons.BACK;
            case "Forward":
                return Input.Buttons.FORWARD;
            default:
                return -1;
        }
    }

    private void loadTouchButtonsFromJSON() {
        JsonValue json = Assets.jsonReader.parse(Gdx.files.internal("json/touch_buttons.json"));
        for (JsonValue key = json.child(); key != null; key = key.next()) {
            float x = key.getFloat("x");
            float y = key.getFloat("y");
            float w = key.getFloat("w");
            float h = key.getFloat("h");
            boolean mouse = Assets.getBooleanFromJson(key, "mouse", false);
            String name = key.getString("key");
            int code = mouse ? getMouseKey(name) : Input.Keys.valueOf(name);
            if (x < 0) {
                x = GP.renderer.getWidth() + x;
            }
            if (y < 0) {
                y = GP.renderer.getHeight() + y;
            }
            Assets.guiMap.put(key.name(), new TouchButton(new Rectangle(x, y, w, h), code, mouse));
        }

    }

    private float transformScreenX(int screenX) {
        return GP.renderer.getWidth() / GameScreen.getWidth() * screenX;
    }

    private float transformScreenY(int screenY) {
        return GP.renderer.getHeight() / GameScreen.getHeight() * screenY;
    }

    private TouchButton getTouchedKey(float touchX, float touchY) {
        for (Entry entry : Assets.guiMap) {
            TouchButton button = (TouchButton) entry.value;
            if (button.getRect().contains(touchX, touchY)) {
                return button;
            }
        }
        return nullButton;
    }

    @Override
    public boolean keyDown(int keycode) {
        GP.input.keyDown(keycode);
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        GP.input.keyUp(keycode);
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float touchX = transformScreenX(screenX);
        float touchY = transformScreenY(screenY);

        if (CaveGame.TOUCH) {
            TouchButton touchedKey = getTouchedKey(touchX, touchY);
            if (touchedKey.isMouse()) {
                GP.input.touchDown(touchX, touchY, touchedKey.getCode());
            } else {
                GP.input.keyDown(touchedKey.getCode());
            }
        } else {
            GP.input.touchDown(touchX, touchY, button);
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        float touchX = transformScreenX(screenX);
        float touchY = transformScreenY(screenY);

        if (CaveGame.TOUCH) {
            TouchButton touchedKey = getTouchedKey(touchX, touchY);
            if (touchedKey.isMouse()) {
                GP.input.touchUp(touchX, touchY, touchedKey.getCode());
            } else {
                GP.input.keyUp(GP.input.getKeyDownCode());
            }
        } else {
            GP.input.touchUp(touchX, touchY, button);
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        float touchX = transformScreenX(screenX);
        float touchY = transformScreenY(screenY);
        if (CaveGame.TOUCH && GP.input.isKeyDown()) {
            if (getTouchedKey(touchX, touchY).getCode() == -1) {
                GP.input.keyUp(GP.input.getKeyDownCode());
            }
        } else {
            GP.input.touchDragged(touchX, touchY);
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        GP.input.scrolled(amount);
        return false;
    }
}
