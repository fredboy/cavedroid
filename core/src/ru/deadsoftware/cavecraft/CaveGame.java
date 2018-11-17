package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import ru.deadsoftware.cavecraft.misc.AppState;

public class CaveGame extends Game {

    public static final String VERSION = "alpha 0.4";
    public static String GAME_FOLDER;

    public static AppState STATE;

    public static boolean TOUCH;

    public CaveGame(String gameFolder) {
        this(gameFolder, false);
    }

    public CaveGame(String gameFolder, boolean touch) {
        GAME_FOLDER = gameFolder;
        TOUCH = touch;
        STATE = AppState.MENU_MAIN;
    }

    @Override
    public void create() {
        Gdx.app.log("CaveGame", GAME_FOLDER);
        Gdx.files.absolute(GAME_FOLDER).mkdirs();
        setScreen(new GameScreen());
    }

}
