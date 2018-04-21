package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import ru.deadsoftware.cavecraft.game.GameProc;
import ru.deadsoftware.cavecraft.menu.MenuRenderer;

public class GameScreen implements Screen {

    public static int FPS;

    private GameProc gameProc;
    private Renderer renderer;

    public GameScreen() {
        Assets.load();
        Items.load();
        gameProc = new GameProc();
        renderer = new MenuRenderer();
        Gdx.input.setInputProcessor(new InputHandlerMenu());
    }

    public static int getWidth() {
        return Gdx.graphics.getWidth();
    }

    public static int getHeight() {
        return Gdx.graphics.getHeight();
    }

    private void game(float delta) {
        gameProc.update(delta);
    }

    private void menu() {
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        FPS = (int)(1/delta);
        switch (CaveGame.STATE) {
            case GAME_PLAY: case GAME_CREATIVE_INV:
                game(delta);
                break;

            case MENU_MAIN:
                menu();
                break;

            case RESTART:
                gameProc = new GameProc();
                Gdx.input.setInputProcessor(new InputHandlerGame(gameProc));
                CaveGame.STATE = GameState.GAME_PLAY;
                break;
        }
        renderer.render();
    }

    @Override
    public void resize(int width, int height) {
        switch (CaveGame.STATE) {
            case MENU_MAIN:
                renderer = new MenuRenderer();
                break;
            case GAME_PLAY: case GAME_CREATIVE_INV:
                gameProc.resetRenderer();
                break;
        }
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
