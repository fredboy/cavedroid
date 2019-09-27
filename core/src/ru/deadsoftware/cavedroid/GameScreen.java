package ru.deadsoftware.cavedroid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import ru.deadsoftware.cavedroid.game.GameItems;
import ru.deadsoftware.cavedroid.game.GameProc;
import ru.deadsoftware.cavedroid.game.GameSaver;
import ru.deadsoftware.cavedroid.menu.MenuRenderer;
import ru.deadsoftware.cavedroid.misc.*;
import ru.deadsoftware.cavedroid.misc.states.AppState;
import ru.deadsoftware.cavedroid.misc.states.GameState;
import ru.deadsoftware.cavedroid.misc.states.MenuState;

public class GameScreen implements Screen {

    public static GameProc GP;
    public static Renderer RENDERER;

    public static int FPS;
    public static boolean SHOW_DEBUG = true;
    public static boolean SHOW_MAP = false;
    public static int NEW_GAME_MODE = 0;

    private MenuRenderer menuRenderer;

    public GameScreen() {
        Assets.load();
        GameItems.load();
        menuRenderer = new MenuRenderer(CaveGame.TOUCH ? 320 : 480);
        RENDERER = menuRenderer;
        Gdx.input.setInputProcessor(new InputHandlerMenu(menuRenderer));
    }

    public static float getWidth() {
        return Gdx.graphics.getWidth();
    }

    public static float getHeight() {
        return Gdx.graphics.getHeight();
    }

    private void game() {
        GP.update();
    }

    private void menu() {
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        FPS = (int) (1 / delta);
        switch (CaveGame.APP_STATE) {
            case GAME:
                game();
                break;

            case MENU:
                menu();
                break;

            case LOAD:
                GP = GameSaver.load();
                GP.resetRenderer();
                RENDERER = GP.renderer;
                Gdx.input.setInputProcessor(new InputHandlerGame());
                CaveGame.APP_STATE = AppState.GAME;
                CaveGame.GAME_STATE = GameState.PLAY;
                break;

            case SAVE:
                GameSaver.save(GP);
                CaveGame.APP_STATE = AppState.MENU;
                CaveGame.MENU_STATE = MenuState.MAIN;
                GP.dispose();
                menuRenderer = new MenuRenderer(CaveGame.TOUCH ? 320 : 480);
                RENDERER = menuRenderer;
                Gdx.input.setInputProcessor(new InputHandlerMenu(menuRenderer));
                break;
        }
        RENDERER.render();
    }

    @Override
    public void resize(int width, int height) {
        switch (CaveGame.APP_STATE) {
            case MENU:
                menuRenderer = new MenuRenderer(CaveGame.TOUCH ? 320 : 480);
                Gdx.input.setInputProcessor(new InputHandlerMenu(menuRenderer));
                RENDERER = menuRenderer;
                break;
            case GAME:
                GP.resetRenderer();
                RENDERER = GP.renderer;
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
