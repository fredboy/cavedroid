package ru.deadsoftware.cavecraft.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ru.deadsoftware.cavecraft.CaveGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.addIcon("icons/icon256.png", Files.FileType.Internal);
		config.addIcon("icons/icon128.png", Files.FileType.Internal);
		config.foregroundFPS = 60;
		config.title = "CaveCraft";
		config.width = 960;
		config.height = 540;

                boolean touch = false;
                for (int i=0; i<arg.length; i++) {
                    if (arg[i].equals("--touch")) touch = true;
                }
		new LwjglApplication(new CaveGame(touch), config);
	}
}
