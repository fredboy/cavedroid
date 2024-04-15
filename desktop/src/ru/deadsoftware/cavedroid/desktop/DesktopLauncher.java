package ru.deadsoftware.cavedroid.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import ru.deadsoftware.cavedroid.CaveGame;

class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowIcon(Files.FileType.Internal, "icons/icon256.png", "icons/icon128.png");
        config.setTitle("CaveDroid");
		config.setWindowedMode(960, 540);
		config.useVsync(true);

		boolean touch = false;
		for (String anArg : arg) {
            if (anArg.equals("--touch")) {
                touch = true;
                break;
            }
		}
        new Lwjgl3Application(new CaveGame(System.getProperty("user.home") + "/.cavedroid", touch), config);
	}
}
