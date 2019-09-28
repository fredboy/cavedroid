package ru.deadsoftware.cavedroid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import ru.deadsoftware.cavedroid.game.GameItems;
import ru.deadsoftware.cavedroid.game.GameProc;
import ru.deadsoftware.cavedroid.game.GameSaver;
import ru.deadsoftware.cavedroid.menu.MenuRenderer;
import ru.deadsoftware.cavedroid.misc.Assets;
import ru.deadsoftware.cavedroid.misc.InputHandlerGame;
import ru.deadsoftware.cavedroid.misc.InputHandlerMenu;
import ru.deadsoftware.cavedroid.misc.Renderer;
import ru.deadsoftware.cavedroid.misc.states.AppState;
import ru.deadsoftware.cavedroid.misc.states.GameState;
import ru.deadsoftware.cavedroid.misc.states.MenuState;

public class GameScreen implements Screen {

    public static GameProc GP;

    public static int FPS;
    public static boolean SHOW_DEBUG = false;
    public static boolean SHOW_MAP = false;

    private Renderer renderer;
    private MenuRenderer menuRenderer;

    private InputHandlerGame inputHandlerGame;
    private final InputHandlerMenu inputHandlerMenu;

    public GameScreen() {
        Assets.load();
        GameItems.load();
        menuRenderer = new MenuRenderer(CaveGame.TOUCH ? 320 : 480);
        inputHandlerMenu = new InputHandlerMenu(menuRenderer);
        renderer = menuRenderer;
        Gdx.input.setInputProcessor(inputHandlerMenu);
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
                GP.resetRenderer();
                renderer = GP.renderer;
                if (inputHandlerGame == null) {
                    inputHandlerGame = new InputHandlerGame();
                }
                Gdx.input.setInputProcessor(inputHandlerGame);
                CaveGame.APP_STATE = AppState.GAME;
                CaveGame.GAME_STATE = GameState.PLAY;
                break;

            case SAVE:
                GameSaver.save(GP);
                CaveGame.APP_STATE = AppState.MENU;
                CaveGame.MENU_STATE = MenuState.MAIN;
                GP.dispose();
                renderer = menuRenderer;
                Gdx.input.setInputProcessor(inputHandlerMenu);
                break;
        }
        renderer.render();
    }

    @Override
    public void resize(int width, int height) {
        switch (CaveGame.APP_STATE) {
            case MENU:
                menuRenderer = new MenuRenderer(CaveGame.TOUCH ? 320 : 480);
                Gdx.input.setInputProcessor(inputHandlerMenu);
                renderer = menuRenderer;
                break;
            case GAME:
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
