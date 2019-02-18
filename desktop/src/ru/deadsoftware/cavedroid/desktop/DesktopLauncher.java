package ru.deadsoftware.cavedroid.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ru.deadsoftware.cavedroid.CaveGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.addIcon("icons/icon256.png", Files.FileType.Internal);
		config.addIcon("icons/icon128.png", Files.FileType.Internal);
		config.foregroundFPS = 60;
        config.title = "CaveDroid";
		config.width = 960;
		config.height = 540;

		boolean touch = false;
		for (String anArg : arg) {
			if (anArg.equals("--touch")) touch = true;
		}
        new LwjglApplication(new CaveGame(System.getProperty("user.home") + "/.cavedroid", touch), config);
	}
}
