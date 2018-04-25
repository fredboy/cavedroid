package ru.deadsoftware.cavecraft.misc;

import com.badlogic.gdx.InputProcessor;
import ru.deadsoftware.cavecraft.GameScreen;
import ru.deadsoftware.cavecraft.menu.MenuRenderer;
import ru.deadsoftware.cavecraft.menu.objects.Button;

public class InputHandlerMenu implements InputProcessor{

    private MenuRenderer renderer;

    public InputHandlerMenu(MenuRenderer renderer) {
        this.renderer = renderer;
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
        screenX *= renderer.getWidth()/ GameScreen.getWidth();
        screenY *= renderer.getHeight()/GameScreen.getHeight();
        for (Button button : renderer.menuMainButtons) {
            if (button.getRect().contains(screenX, screenY) && button.getType()>0) renderer.buttonClicked(button);
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
