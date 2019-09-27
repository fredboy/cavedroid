package ru.deadsoftware.cavedroid;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import ru.deadsoftware.cavedroid.misc.states.AppState;
import ru.deadsoftware.cavedroid.misc.states.GameState;
import ru.deadsoftware.cavedroid.misc.states.MenuState;

public class CaveGame extends Game {

    public static final String VERSION = "alpha 0.4";

    public static AppState APP_STATE;
    public static GameState GAME_STATE;
    public static MenuState MENU_STATE;

    public static String GAME_FOLDER;
    public static boolean TOUCH;

    public CaveGame(String gameFolder) {
        this(gameFolder, false);
    }

    public CaveGame(String gameFolder, boolean touch) {
        GAME_FOLDER = gameFolder;
        TOUCH = touch;
        APP_STATE = AppState.MENU;
        MENU_STATE = MenuState.MAIN;
        GAME_STATE = GameState.PLAY;
    }

    @Override
    public void create() {
        Gdx.app.log("CaveGame", GAME_FOLDER);
        Gdx.files.absolute(GAME_FOLDER).mkdirs();
        setScreen(new GameScreen());
    }

}
