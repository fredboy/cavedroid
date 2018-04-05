package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import ru.deadsoftware.cavecraft.game.GameInputHandler;
import ru.deadsoftware.cavecraft.game.GameProc;
import ru.deadsoftware.cavecraft.game.GameRenderer;

public class GameScreen implements Screen {

    private GameProc gameProc;
    private GameInputHandler gameInput;

    public GameScreen() {
        gameProc = new GameProc();
        gameInput = new GameInputHandler(gameProc);

        Gdx.input.setInputProcessor(new InputHandler());
    }

    public static int getWidth() {
        return Gdx.graphics.getWidth();
    }

    public static int getHeight() {
        return Gdx.graphics.getHeight();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        gameProc.update(delta);
        gameProc.renderer.render();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    private class InputHandler implements InputProcessor {

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
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            gameInput.touchDown(screenX, screenY, button);
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            gameInput.touchUp(screenX, screenY, button);
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            gameInput.touchDragged(screenX, screenY);
            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            gameInput.mouseMoved(screenX,screenY);
            return false;
        }

        @Override
        public boolean scrolled(int amount) {
            return false;
        }
    }
}
