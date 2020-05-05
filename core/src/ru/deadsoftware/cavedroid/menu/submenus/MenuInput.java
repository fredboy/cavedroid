package ru.deadsoftware.cavedroid.menu.submenus;

import com.badlogic.gdx.Gdx;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.game.GameSaver;
import ru.deadsoftware.cavedroid.misc.states.AppState;
import ru.deadsoftware.cavedroid.misc.states.MenuState;

class MenuInput {

    private static void startNewGame(int gameMode) {
//        GP = new GameProc();
//        GP.player.respawn();
//        GameSaver.save(GP);
//        CaveGame.APP_STATE = AppState.LOAD;
    }

    static void newGameClicked() {
//        CaveGame.MENU_STATE = MenuState.NEW_GAME;
    }

    static void loadGameClicked() {
//        CaveGame.APP_STATE = AppState.LOAD;
//        GP = GameSaver.load();
    }

    static void quitClicked() {
        Gdx.app.exit();
    }

    static void survivalClicked() {
        startNewGame(0);
    }

    static void creativeClicked() {
        startNewGame(1);
    }

    static void backClicked() {
//        CaveGame.MENU_STATE = MenuState.MAIN;
    }

}
