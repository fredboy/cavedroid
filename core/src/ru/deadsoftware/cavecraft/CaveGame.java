package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class CaveGame extends Game {

	public static final String VERSION = "alpha 0.3";
	public static String GAME_FOLDER;

	public static AppState STATE;

	public static boolean TOUCH;

	public CaveGame() {
		this(false);
	}

	public CaveGame(boolean touch) {
		TOUCH = touch;
		STATE = AppState.MENU_MAIN;
	}

	@Override
	public void create () {
		switch (Gdx.app.getType()) {
			case Desktop:
				GAME_FOLDER = System.getProperty("user.home")+"/.cavecraft";
				break;
			case Android:
				GAME_FOLDER = "/sdcard/cavecraft";
				break;
			default:
				Gdx.app.exit();
		}
		Gdx.app.log("CaveGame", "Folder: "+GAME_FOLDER);
		Gdx.files.local(GAME_FOLDER).mkdirs();
		setScreen(new GameScreen());
	}

}
