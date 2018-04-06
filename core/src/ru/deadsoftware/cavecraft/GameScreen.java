package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import ru.deadsoftware.cavecraft.game.GameInputHandler;
import ru.deadsoftware.cavecraft.game.GameProc;

public class GameScreen implements Screen {

    private GameProc gameProc;
    private GameInputHandler gameInput;

    public GameScreen() {
        Assets.load();
        Items.load();
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
        gameProc.resetRenderer();
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
            screenX *= gameProc.renderer.camera.viewportWidth/getWidth();
            screenY *= gameProc.renderer.camera.viewportHeight/getHeight();
            gameInput.touchDown(screenX, screenY, button);
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            screenX *= gameProc.renderer.camera.viewportWidth/getWidth();
            screenY *= gameProc.renderer.camera.viewportHeight/getHeight();
            gameInput.touchUp(screenX, screenY, button);
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
}
