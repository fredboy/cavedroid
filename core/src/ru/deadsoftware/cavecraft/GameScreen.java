package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import ru.deadsoftware.cavecraft.game.GameItems;
import ru.deadsoftware.cavecraft.game.GameProc;
import ru.deadsoftware.cavecraft.game.GameSaver;
import ru.deadsoftware.cavecraft.menu.MenuRenderer;
import ru.deadsoftware.cavecraft.misc.*;

public class GameScreen implements Screen {

    public static int FPS;
    public static boolean SHOW_DEBUG = false;
    public static int NEW_GAME_MODE = 0;

    public static GameProc GP;

    private Renderer renderer;
    private MenuRenderer menuRenderer;

    public GameScreen() {
        Assets.load();
        GameItems.load();
        menuRenderer = new MenuRenderer(CaveGame.TOUCH ? 320 : 480);
        renderer = menuRenderer;
        Gdx.input.setInputProcessor(new InputHandlerMenu(menuRenderer));
    }

    public static int getWidth() {
        return Gdx.graphics.getWidth();
    }

    public static int getHeight() {
        return Gdx.graphics.getHeight();
    }

    private void game(float delta) {
        GP.update(delta);
    }

    private void menu() {
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        FPS = (int) (1 / delta);
        switch (CaveGame.STATE) {
            case GAME_PLAY:
            case GAME_CREATIVE_INV:
                game(delta);
                break;

            case MENU_MAIN:
                menu();
                break;

            case NEW_GAME:
                GP = new GameProc();
                GP.initGame(NEW_GAME_MODE);
                renderer = GP.renderer;
                Gdx.input.setInputProcessor(new InputHandlerGame());
                CaveGame.STATE = AppState.GAME_PLAY;
                break;

            case LOAD_GAME:
                GP = GameSaver.load();
                renderer = GP.renderer;
                Gdx.input.setInputProcessor(new InputHandlerGame());
                CaveGame.STATE = AppState.GAME_PLAY;
                break;

            case SAVE_GAME:
                GameSaver.save(GP);
                CaveGame.STATE = AppState.MENU_MAIN;
                break;

            case GOTO_MENU:
                menuRenderer = new MenuRenderer(CaveGame.TOUCH ? 320 : 480);
                renderer = menuRenderer;
                Gdx.input.setInputProcessor(new InputHandlerMenu(menuRenderer));
                break;
        }
        renderer.render();
    }

    @Override
    public void resize(int width, int height) {
        switch (CaveGame.STATE) {
            case MENU_MAIN:

                menuRenderer = new MenuRenderer(CaveGame.TOUCH ? 320 : 480);
                renderer = menuRenderer;
                break;
            case GAME_PLAY:
            case GAME_CREATIVE_INV:
                GP.resetRenderer();
                renderer = GP.renderer;
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
