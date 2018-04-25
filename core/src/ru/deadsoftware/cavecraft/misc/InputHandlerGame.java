package ru.deadsoftware.cavecraft.misc;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import ru.deadsoftware.cavecraft.CaveGame;
import ru.deadsoftware.cavecraft.game.GameInput;
import ru.deadsoftware.cavecraft.game.GameProc;

import static ru.deadsoftware.cavecraft.GameScreen.getHeight;
import static ru.deadsoftware.cavecraft.GameScreen.getWidth;

public class InputHandlerGame implements InputProcessor {

    private GameProc gameProc;
    private GameInput gameInput;

    private float tWidth, tHeight;

    public InputHandlerGame(GameProc gameProc) {
        this.gameProc = gameProc;
        this.gameInput = new GameInput(gameProc);
        tWidth = gameProc.renderer.camera.viewportWidth;
        tHeight = gameProc.renderer.camera.viewportHeight;
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
        int tx = (int)(screenX * tWidth/getWidth());
        int ty = (int)(screenY * tHeight/getHeight());
        screenX *= gameProc.renderer.camera.viewportWidth/getWidth();
        screenY *= gameProc.renderer.camera.viewportHeight/getHeight();

        if (CaveGame.TOUCH) {
            if (tx > 26 && tx < 52 && ty > tHeight - 52 && ty < tHeight - 26) {
                if (gameProc.ctrlMode==1) gameInput.keyDown(Input.Keys.W);
                    else gameInput.keyDown(Input.Keys.SPACE);
            } else if (tx > 0 && tx < 26 && ty > tHeight - 26) {
                gameInput.keyDown(Input.Keys.A);
            } else if (tx > 26 && tx < 52 && ty > tHeight - 26) {
                if (gameProc.ctrlMode==1) gameInput.keyDown(Input.Keys.S);
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
                gameInput.touchDown(screenX, screenY, Input.Buttons.LEFT);
            }
        } else {
            gameInput.touchDown(screenX, screenY, button);
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        int tx = (int)(screenX * tWidth/getWidth());
        int ty = (int)(screenY * tHeight/getHeight());
        screenX *= gameProc.renderer.camera.viewportWidth/getWidth();
        screenY *= gameProc.renderer.camera.viewportHeight/getHeight();
        if (CaveGame.TOUCH) {
            if (tx > 26 && tx < 52 && ty > tHeight - 52 && ty < tHeight - 26) {
                if (gameProc.ctrlMode==1) gameInput.keyUp(Input.Keys.W);
                else gameInput.keyUp(Input.Keys.SPACE);
            } else if (tx > 0 && tx < 26 && ty > tHeight - 26) {
                gameInput.keyUp(Input.Keys.A);
            } else if (tx > 26 && tx < 52 && ty > tHeight - 26) {
                if (gameProc.ctrlMode==1) gameInput.keyUp(Input.Keys.S);
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
                gameInput.touchUp(screenX, screenY, Input.Buttons.LEFT);
            }
        } else {
            gameInput.touchUp(screenX, screenY, button);
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        screenX *= gameProc.renderer.camera.viewportWidth/getWidth();
        screenY *= gameProc.renderer.camera.viewportHeight/getHeight();
        gameInput.touchDragged(screenX, screenY);
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        screenX *= gameProc.renderer.camera.viewportWidth/getWidth();
        screenY *= gameProc.renderer.camera.viewportHeight/getHeight();
        gameInput.mouseMoved(screenX,screenY);
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        gameInput.scrolled(amount);
        return false;
    }
}