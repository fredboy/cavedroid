package ru.deadsoftware.cavedroid.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import ru.deadsoftware.cavedroid.CaveGame;

class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowIcon(Files.FileType.Internal, "icons/icon256.png", "icons/icon128.png");
        config.setTitle("CaveDroid");
        config.setWindowedMode(960, 540);
        config.useVsync(true);

        boolean touch = false;
        boolean debug = false;
        String assetsPath = null;

        for (String anArg : arg) {
            if (anArg.equals("--touch")) {
                touch = true;
            }

            if (anArg.equals("--debug")) {
                debug = true;
            }

            if (anArg.startsWith("--assets")) {
                String[] splitArg = anArg.split("=");
                if (splitArg.length >= 2) {
                    assetsPath = splitArg[1];
                }
            }
        }

        CaveGame caveGame = new CaveGame(System.getProperty("user.home") + "/.cavedroid", touch, assetsPath);
        caveGame.setDebug(debug);

        new Lwjgl3Application(caveGame, config);
    }
}
