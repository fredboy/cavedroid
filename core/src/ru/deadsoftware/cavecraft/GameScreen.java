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

    public GameScreen() {
        Assets.load();
        Items.load();
        gameProc = new GameProc();
        Gdx.input.setInputProcessor(new InputHandler(gameProc));
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

}
