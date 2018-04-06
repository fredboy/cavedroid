package ru.deadsoftware.cavecraft.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ru.deadsoftware.cavecraft.CaveGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.foregroundFPS = 60;
		config.title = "CaveCraft";
		config.width = 960;
		config.height = 720;
		new LwjglApplication(new CaveGame(), config);
	}
}
