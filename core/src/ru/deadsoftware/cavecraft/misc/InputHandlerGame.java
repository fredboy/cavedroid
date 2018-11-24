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

    public InputHandlerGame(GameProc gp) {
        this.gp = gp;
        this.gameInput = new GameInput(gp);
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
        screenX *= gp.renderer.getWidth() / getWidth();
        screenY *= gp.renderer.getHeight() / getHeight();

        if (CaveGame.TOUCH) {
            if (screenX > 26 && screenX < 52 && screenY > gp.renderer.getHeight() - 52 &&
                    screenY < gp.renderer.getHeight() - 26) {
                if (gp.ctrlMode == 1) gameInput.keyDown(Input.Keys.W);
                else gameInput.keyDown(Input.Keys.SPACE);
            } else if (screenX > 0 && screenX < 26 && screenY > gp.renderer.getHeight() - 26) {
                gameInput.keyDown(Input.Keys.A);
            } else if (screenX > 26 && screenX < 52 && screenY > gp.renderer.getHeight() - 26) {
                if (gp.ctrlMode == 1) gameInput.keyDown(Input.Keys.S);
                else gameInput.keyDown(Input.Keys.CONTROL_LEFT);
            } else if (screenX > 52 && screenX < 78 && screenY > gp.renderer.getHeight() - 26) {
                gameInput.keyDown(Input.Keys.D);
            } else if (screenX > 78 && screenX < 104 && screenY > gp.renderer.getHeight() - 26) {
                gameInput.keyDown(Input.Keys.ALT_LEFT);
            } else if (screenX > gp.renderer.getWidth() - 52 && screenX < gp.renderer.getWidth() - 26 &&
                    screenY > gp.renderer.getHeight() - 26) {
                gameInput.touchDown(screenX, screenY, Input.Buttons.LEFT);
            } else if (screenX > gp.renderer.getWidth() - 26 && screenY > screenY - 26) {
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
        screenX *= gp.renderer.getWidth() / getWidth();
        screenY *= gp.renderer.getHeight() / getHeight();
        if (CaveGame.TOUCH) {
            if (screenX > 26 && screenX < 52 && screenY > gp.renderer.getHeight() - 52 &&
                    screenY < gp.renderer.getHeight() - 26) {
                if (gp.ctrlMode == 1) gameInput.keyUp(Input.Keys.W);
                else gameInput.keyUp(Input.Keys.SPACE);
            } else if (screenX > 0 && screenX < 26 && screenY > gp.renderer.getHeight() - 26) {
                gameInput.keyUp(Input.Keys.A);
            } else if (screenX > 26 && screenX < 52 && screenY > gp.renderer.getHeight() - 26) {
                if (gp.ctrlMode == 1) gameInput.keyUp(Input.Keys.S);
                else gameInput.keyUp(Input.Keys.CONTROL_LEFT);
            } else if (screenX > 52 && screenX < 78 && screenY > gp.renderer.getHeight() - 26) {
                gameInput.keyUp(Input.Keys.D);
            } else if (screenX > 78 && screenX < 104 && screenY > gp.renderer.getHeight() - 26) {
                gameInput.keyUp(Input.Keys.ALT_LEFT);
            } else if (screenX > gp.renderer.getWidth() - 52 && screenX < gp.renderer.getWidth() - 26 &&
                    screenY > gp.renderer.getHeight() - 26) {
                gameInput.touchUp(screenX, screenY, Input.Buttons.LEFT);
            } else if (screenX > gp.renderer.getWidth() - 26 && screenY > screenY - 26) {
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