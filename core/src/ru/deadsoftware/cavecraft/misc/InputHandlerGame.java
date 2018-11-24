package ru.deadsoftware.cavecraft.misc;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import ru.deadsoftware.cavecraft.CaveGame;
import ru.deadsoftware.cavecraft.game.GameInput;
import ru.deadsoftware.cavecraft.game.GameProc;

import static ru.deadsoftware.cavecraft.GameScreen.getHeight;
import static ru.deadsoftware.cavecraft.GameScreen.getWidth;

public class InputHandlerGame implements InputProcessor {

    private GameProc gp;
    private GameInput gameInput;

    private float tWidth, tHeight;

    public InputHandlerGame(GameProc gp) {
        this.gp = gp;
        this.gameInput = new GameInput(gp);
        tWidth = gp.renderer.getWidth();
        tHeight = gp.renderer.getHeight();
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
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        int tx = (int) (screenX * tWidth / getWidth());
        int ty = (int) (screenY * tHeight / getHeight());
        screenX *= gp.renderer.getWidth() / getWidth();
        screenY *= gp.renderer.getHeight() / getHeight();

        if (CaveGame.TOUCH) {
            if (tx > 26 && tx < 52 && ty > tHeight - 52 && ty < tHeight - 26) {
                if (gp.ctrlMode == 1) gameInput.keyDown(Input.Keys.W);
                else gameInput.keyDown(Input.Keys.SPACE);
            } else if (tx > 0 && tx < 26 && ty > tHeight - 26) {
                gameInput.keyDown(Input.Keys.A);
            } else if (tx > 26 && tx < 52 && ty > tHeight - 26) {
                if (gp.ctrlMode == 1) gameInput.keyDown(Input.Keys.S);
                else gameInput.keyDown(Input.Keys.CONTROL_LEFT);
            } else if (tx > 52 && tx < 78 && ty > tHeight - 26) {
                gameInput.keyDown(Input.Keys.D);
            } else if (tx > 78 && tx < 104 && ty > tHeight - 26) {
                gameInput.keyDown(Input.Keys.ALT_LEFT);
            } else if (tx > tWidth - 52 && tx < tWidth - 26 && ty > tHeight - 26) {
                gameInput.touchDown(screenX, screenY, Input.Buttons.LEFT);
            } else if (tx > tWidth - 26 && screenY > ty - 26) {
                gameInput.touchDown(screenX, screenY, Input.Buttons.RIGHT);
            } else {
                gameInput.touchDown(screenX, screenY, -1);
            }
        } else {
            gameInput.touchDown(screenX, screenY, button);
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        int tx = (int) (screenX * tWidth / getWidth());
        int ty = (int) (screenY * tHeight / getHeight());
        screenX *= gp.renderer.getWidth() / getWidth();
        screenY *= gp.renderer.getHeight() / getHeight();
        if (CaveGame.TOUCH) {
            if (tx > 26 && tx < 52 && ty > tHeight - 52 && ty < tHeight - 26) {
                if (gp.ctrlMode == 1) gameInput.keyUp(Input.Keys.W);
                else gameInput.keyUp(Input.Keys.SPACE);
            } else if (tx > 0 && tx < 26 && ty > tHeight - 26) {
                gameInput.keyUp(Input.Keys.A);
            } else if (tx > 26 && tx < 52 && ty > tHeight - 26) {
                if (gp.ctrlMode == 1) gameInput.keyUp(Input.Keys.S);
                else gameInput.keyUp(Input.Keys.CONTROL_LEFT);
            } else if (tx > 52 && tx < 78 && ty > tHeight - 26) {
                gameInput.keyUp(Input.Keys.D);
            } else if (tx > 78 && tx < 104 && ty > tHeight - 26) {
                gameInput.keyUp(Input.Keys.ALT_LEFT);
            } else if (tx > tWidth - 52 && tx < tWidth - 26 && ty > tHeight - 26) {
                gameInput.touchUp(screenX, screenY, Input.Buttons.LEFT);
            } else if (tx > tWidth - 26 && screenY > ty - 26) {
                gameInput.touchUp(screenX, screenY, Input.Buttons.RIGHT);
            } else {
                gameInput.touchUp(screenX, screenY, -1);
            }
        } else {
            gameInput.touchUp(screenX, screenY, button);
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        screenX *= gp.renderer.getWidth() / getWidth();
        screenY *= gp.renderer.getHeight() / getHeight();
        if (gp.isKeyDown && (screenX > 78 || screenY < gp.renderer.getHeight() - 52)) {
            gameInput.keyUp(gp.keyDownCode);
        } else {
            gameInput.touchDragged(screenX, screenY);
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        gameInput.scrolled(amount);
        return false;
    }
}