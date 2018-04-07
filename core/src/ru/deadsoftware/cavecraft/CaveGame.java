package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.Game;

public class CaveGame extends Game {

	public static boolean TOUCH;

	public CaveGame() {
		this(false);
	}

	public CaveGame(boolean touch) {
		TOUCH = touch;
	}

	@Override
	public void create () {
		setScreen(new GameScreen());
	}

}
