package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.objects.TouchButton;
import ru.deadsoftware.cavedroid.misc.Assets;

import javax.inject.Inject;

import static com.badlogic.gdx.utils.ObjectMap.Entry;

@GameScope
public class GameInputProcessor extends InputAdapter {

    private static final TouchButton nullButton = new TouchButton(null, -1, true);

    private final GameInput mGameInput;
    private final GameRenderer mGameRenderer;
    private final MainConfig mMainConfig;

    @Inject
    public GameInputProcessor(GameInput gameInput,
                              GameRenderer gameRenderer,
                              MainConfig mainConfig) {
        mGameInput = gameInput;
        mGameRenderer = gameRenderer;
        mMainConfig = mainConfig;

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
                x = mGameRenderer.getWidth() + x;
            }
            if (y < 0) {
                y = mGameRenderer.getHeight() + y;
            }
            Assets.guiMap.put(key.name(), new TouchButton(new Rectangle(x, y, w, h), code, mouse));
        }

    }

    private float transformScreenX(int screenX) {
        return mGameRenderer.getWidth() / Gdx.graphics.getWidth() * screenX;
    }

    private float transformScreenY(int screenY) {
        return mGameRenderer.getHeight() / Gdx.graphics.getHeight() * screenY;
    }

    private TouchButton getTouchedKey(float touchX, float touchY) {
        for (Entry<String, TouchButton> entry : Assets.guiMap) {
            TouchButton button = entry.value;
            if (button.getRect().contains(touchX, touchY)) {
                return button;
            }
        }
        return nullButton;
    }

    @Override
    public boolean keyDown(int keycode) {
        mGameInput.keyDown(keycode);
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        mGameInput.keyUp(keycode);
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float touchX = transformScreenX(screenX);
        float touchY = transformScreenY(screenY);

        if (mMainConfig.isTouch()) {
            TouchButton touchedKey = getTouchedKey(touchX, touchY);
            if (touchedKey.isMouse()) {
                mGameInput.touchDown(touchX, touchY, touchedKey.getCode());
            } else {
                mGameInput.keyDown(touchedKey.getCode());
            }
        } else {
            mGameInput.touchDown(touchX, touchY, button);
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        float touchX = transformScreenX(screenX);
        float touchY = transformScreenY(screenY);

        if (mMainConfig.isTouch()) {
            TouchButton touchedKey = getTouchedKey(touchX, touchY);
            if (touchedKey.isMouse()) {
                mGameInput.touchUp(touchX, touchY, touchedKey.getCode());
            } else {
                mGameInput.keyUp(mGameInput.getKeyDownCode());
            }
        } else {
            mGameInput.touchUp(touchX, touchY, button);
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        float touchX = transformScreenX(screenX);
        float touchY = transformScreenY(screenY);
        if (mMainConfig.isTouch() && mGameInput.isKeyDown()) {
            if (getTouchedKey(touchX, touchY).getCode() == -1) {
                mGameInput.keyUp(mGameInput.getKeyDownCode());
            }
        } else {
            mGameInput.touchDragged(touchX, touchY);
        }
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        mGameInput.scrolled(amountX, amountY);
        return false;
    }
}
