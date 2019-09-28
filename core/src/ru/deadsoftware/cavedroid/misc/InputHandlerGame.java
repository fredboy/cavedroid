package ru.deadsoftware.cavedroid.misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.GameScreen;
import ru.deadsoftware.cavedroid.game.GameInput;

import static ru.deadsoftware.cavedroid.GameScreen.GP;

public class InputHandlerGame extends InputAdapter {

    private static final int
            UP = 0,
            DOWN = 1,
            LEFT = 2,
            RIGHT = 3,
            ALT = 4,
            LMB = 5,
            RMB = 6;

    private GameInput gameInput;

    public InputHandlerGame() {
        this.gameInput = new GameInput();
        loadTouchButtonsFromJSON();
    }

    private void loadTouchButtonsFromJSON() {
        JsonValue json = Assets.jsonReader.parse(Gdx.files.internal("json/touch_buttons.json"));
        for (JsonValue key = json.child(); key != null; key = key.next()) {
            float x = key.getFloat("x");
            float y = key.getFloat("y");
            float w = key.getFloat("w");
            float h = key.getFloat("h");
            if (x < 0) x = GP.renderer.getWidth() + x;
            if (y < 0) y = GP.renderer.getHeight() + y;
            Assets.guiMap.put(key.name(), new Rectangle(x, y, w, h));
        }

    }

    private float transformScreenX(int screenX) {
        return GP.renderer.getWidth() / GameScreen.getWidth() * screenX;
    }

    private float transformScreenY(int screenY) {
        return GP.renderer.getHeight() / GameScreen.getHeight() * screenY;
    }

    private int getTouchedKey(float touchX, float touchY) {
        for (int i = 0; i < Assets.guiMap.size; i++) {
            if (Assets.guiMap.getValueAt(i).contains(touchX, touchY)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean keyDown(int keycode) {
        gameInput.keyDown(keycode);
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        gameInput.keyUp(keycode);
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float touchX = transformScreenX(screenX);
        float touchY = transformScreenY(screenY);

        if (CaveGame.TOUCH) {
            int touchedKey = getTouchedKey(touchX, touchY);
            switch (touchedKey) {
                case UP:
                    gameInput.keyDown(GP.controlMode == ControlMode.CURSOR ? Input.Keys.W : Input.Keys.SPACE);
                    break;
                case DOWN:
                    gameInput.keyDown(GP.controlMode == ControlMode.CURSOR ? Input.Keys.S : Input.Keys.CONTROL_LEFT);
                    break;
                case LEFT:
                    gameInput.keyDown(Input.Keys.A);
                    break;
                case RIGHT:
                    gameInput.keyDown(Input.Keys.D);
                    break;
                case ALT:
                    gameInput.keyDown(Input.Keys.ALT_LEFT);
                    break;
                case LMB:
                    gameInput.touchDown(touchX, touchY, Input.Buttons.LEFT);
                    break;
                case RMB:
                    gameInput.touchDown(touchX, touchY, Input.Buttons.RIGHT);
                    break;
                default:
                    gameInput.touchDown(touchX, touchY, touchedKey);
            }
        } else {
            gameInput.touchDown(touchX, touchY, button);
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        float touchX = transformScreenX(screenX);
        float touchY = transformScreenY(screenY);

        if (CaveGame.TOUCH) {
            int touchedKey = getTouchedKey(touchX, touchY);
            switch (touchedKey) {
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                case ALT:
                    gameInput.keyUp(GP.keyDownCode);
                    break;
                case LMB:
                    gameInput.touchUp(touchX, touchY, Input.Buttons.LEFT);
                    break;
                case RMB:
                    gameInput.touchUp(touchX, touchY, Input.Buttons.RIGHT);
                    break;
                default:
                    gameInput.touchUp(touchX, touchY, touchedKey);
            }
        } else {
            gameInput.touchUp(touchX, touchY, button);
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        float touchX = transformScreenX(screenX);
        float touchY = transformScreenY(screenY);
        if (CaveGame.TOUCH && GP.isKeyDown) {
            if (getTouchedKey(touchX, touchY) == -1) {
                gameInput.keyUp(GP.keyDownCode);
            }
        } else {
            gameInput.touchDragged(touchX, touchY);
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        gameInput.scrolled(amount);
        return false;
    }
}
