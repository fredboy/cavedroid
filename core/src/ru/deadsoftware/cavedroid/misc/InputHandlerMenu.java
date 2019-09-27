package ru.deadsoftware.cavedroid.misc;

import com.badlogic.gdx.InputProcessor;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.GameScreen;
import ru.deadsoftware.cavedroid.menu.MenuRenderer;
import ru.deadsoftware.cavedroid.menu.objects.Button;

public class InputHandlerMenu implements InputProcessor {

    private MenuRenderer menuRenderer;

    public InputHandlerMenu(MenuRenderer menuRenderer) {
        this.menuRenderer = menuRenderer;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int mb) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int mb) {
        screenX *= menuRenderer.getWidth() / GameScreen.getWidth();
        screenY *= menuRenderer.getHeight() / GameScreen.getHeight();
        switch (CaveGame.MENU_STATE) {
            case MAIN:
                for (Button button : menuRenderer.menuMainBtns) {
                    if (button.getRect().contains(screenX, screenY) && button.getType() > 0) {
                        menuRenderer.buttonClicked(button);
                        break;
                    }
                }
                break;
            case NEW_GAME:
                for (Button button : menuRenderer.menuNGBtns) {
                    if (button.getRect().contains(screenX, screenY) && button.getType() > 0) {
                        menuRenderer.buttonClicked(button);
                        break;
                    }
                }
                break;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

}
