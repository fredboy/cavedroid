package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.Game;

public class CaveGame extends Game {

	public static final String VERSION = "alpha 0.2.1";

	public static GameState STATE;

	public static boolean TOUCH;

	public CaveGame() {
		this(false);
	}

	public CaveGame(boolean touch) {
		TOUCH = touch;
		STATE = GameState.MENU_MAIN;
	}

	@Override
	public void create () {
		setScreen(new GameScreen());
	}

}
