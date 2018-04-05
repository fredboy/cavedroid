package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.Game;

public class CaveGame extends Game {

	@Override
	public void create () {
		setScreen(new GameScreen());
	}

}