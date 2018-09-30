package ru.deadsoftware.cavecraft.misc;

import com.badlogic.gdx.InputProcessor;
import ru.deadsoftware.cavecraft.CaveGame;
import ru.deadsoftware.cavecraft.GameScreen;
import ru.deadsoftware.cavecraft.menu.MenuRenderer;
import ru.deadsoftware.cavecraft.menu.objects.Button;

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
        switch (CaveGame.STATE) {
            case MENU_MAIN:
                for (Button button : menuRenderer.menuMainBtns) {
                    if (button.getRect().contains(screenX, screenY) && button.getType() > 0)
                        menuRenderer.buttonClicked(button);
                }
                break;
            case MENU_NEW_GAME:
                for (Button button : menuRenderer.menuNGBtns) {
                    if (button.getRect().contains(screenX, screenY) && button.getType() > 0)
                        menuRenderer.buttonClicked(button);
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
