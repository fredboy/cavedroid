package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import ru.deadsoftware.cavecraft.game.GameInputHandler;
import ru.deadsoftware.cavecraft.game.GameProc;

public class GameScreen implements Screen {

    public static int FPS;

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
        FPS = (int)(1/delta);
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
            if (keycode == Input.Keys.N) {
                gameProc = new GameProc();
                gameInput = new GameInputHandler(gameProc);
            } else {
                gameInput.keyDown(keycode);
            }
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
            if (CaveGame.TOUCH) {
                if (screenX > 26 && screenX < 52 &&
                        screenY > gameProc.renderer.camera.viewportHeight - 52 &&
                        screenY < gameProc.renderer.camera.viewportHeight - 26) {
                    gameInput.keyDown(Input.Keys.W);
                } else if (screenX > 0 && screenX < 26 &&
                        screenY > gameProc.renderer.camera.viewportHeight - 26) {
                    gameInput.keyDown(Input.Keys.A);
                } else if (screenX > 26 && screenX < 52 &&
                        screenY > gameProc.renderer.camera.viewportHeight - 26) {
                    gameInput.keyDown(Input.Keys.S);
                } else if (screenX > 52 && screenX < 78 &&
                        screenY > gameProc.renderer.camera.viewportHeight - 26) {
                    gameInput.keyDown(Input.Keys.D);
                } else if (screenX > 78 && screenX < 104 &&
                        screenY > gameProc.renderer.camera.viewportHeight - 26) {
                    gameInput.keyDown(Input.Keys.ALT_LEFT);
                } else if (screenX > gameProc.renderer.camera.viewportWidth - 52 &&
                        screenX < gameProc.renderer.camera.viewportWidth - 26 &&
                        screenY > gameProc.renderer.camera.viewportHeight - 26) {
                    gameInput.touchDown(screenX, screenY, Input.Buttons.LEFT);
                } else if (screenX > gameProc.renderer.camera.viewportWidth - 26 &&
                        screenY > gameProc.renderer.camera.viewportHeight - 26) {
                    gameInput.touchDown(screenX, screenY, Input.Buttons.RIGHT);
                } else if (screenX > gameProc.renderer.camera.viewportWidth / 2 - 52 &&
                        screenX < gameProc.renderer.camera.viewportWidth / 2 + 52 &&
                        screenY > gameProc.renderer.camera.viewportHeight - 26) {
                    gameInput.keyDown(Input.Keys.SPACE);
                }
            } else {
                gameInput.touchDown(screenX, screenY, button);
            }
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            screenX *= gameProc.renderer.camera.viewportWidth/getWidth();
            screenY *= gameProc.renderer.camera.viewportHeight/getHeight();
            if (CaveGame.TOUCH) {
                if (screenX>26 && screenX<52 &&
                        screenY>gameProc.renderer.camera.viewportHeight-52 &&
                        screenY<gameProc.renderer.camera.viewportHeight-26) {
                    gameInput.keyUp(Input.Keys.W);
                } else if (screenX>0 && screenX<26 &&
                        screenY>gameProc.renderer.camera.viewportHeight-26) {
                    gameInput.keyUp(Input.Keys.A);
                } else if (screenX>26 && screenX<52 &&
                        screenY>gameProc.renderer.camera.viewportHeight-26) {
                    gameInput.keyUp(Input.Keys.S);
                } else if (screenX>52 && screenX<78 &&
                        screenY>gameProc.renderer.camera.viewportHeight-26) {
                    gameInput.keyUp(Input.Keys.D);
                } else if (screenX > 78 && screenX < 104 &&
                        screenY > gameProc.renderer.camera.viewportHeight - 26) {
                    gameInput.keyUp(Input.Keys.ALT_LEFT);
                } else if (screenX > gameProc.renderer.camera.viewportWidth - 52 &&
                        screenX < gameProc.renderer.camera.viewportWidth - 26 &&
                        screenY > gameProc.renderer.camera.viewportHeight - 26) {
                    gameInput.touchUp(screenX, screenY, Input.Buttons.LEFT);
                } else if (screenX > gameProc.renderer.camera.viewportWidth - 26 &&
                        screenY > gameProc.renderer.camera.viewportHeight - 26) {
                    gameInput.touchUp(screenX, screenY, Input.Buttons.RIGHT);
                } else if (screenX > gameProc.renderer.camera.viewportWidth / 2 - 52 &&
                        screenX < gameProc.renderer.camera.viewportWidth / 2 + 52 &&
                        screenY > gameProc.renderer.camera.viewportHeight - 26) {
                    gameInput.keyUp(Input.Keys.SPACE);
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
}
