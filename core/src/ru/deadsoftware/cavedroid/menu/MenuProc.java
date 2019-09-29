package ru.deadsoftware.cavedroid.menu;

import com.badlogic.gdx.utils.ObjectMap;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.GameScreen;
import ru.deadsoftware.cavedroid.menu.objects.Button;
import ru.deadsoftware.cavedroid.menu.submenus.Menu;
import ru.deadsoftware.cavedroid.menu.submenus.MenuMain;
import ru.deadsoftware.cavedroid.menu.submenus.MenuNewGame;
import ru.deadsoftware.cavedroid.misc.Renderer;
import ru.deadsoftware.cavedroid.misc.states.AppState;
import ru.deadsoftware.cavedroid.misc.states.GameState;
import ru.deadsoftware.cavedroid.misc.states.MenuState;

import static ru.deadsoftware.cavedroid.misc.Assets.*;

public class MenuProc extends Renderer {

    private MenuMain mainMenu;
    private MenuNewGame newGameMenu;

    private Menu currentMenu;

    public MenuProc(int width) {
        super(width, width * GameScreen.getHeight() / GameScreen.getWidth());
        mainMenu = new MenuMain(getWidth(), getHeight(), this::drawButton);
        newGameMenu = new MenuNewGame(getWidth(), getHeight(), this::drawButton);
        currentMenu = mainMenu;
    }

    private void drawButton(Button button) {
        spriter.draw(textureRegions.get("button_" + button.getType()), button.getX(), button.getY());
        setFontColor(255, 255, 255);
        drawString(button.getLabel(),
                (button.getX() + button.getWidth() / 2) - (float) getStringWidth(button.getLabel()) / 2,
                (button.getY() + button.getHeight() / 2) - (float) getStringHeight(button.getLabel()) / 2);
    }

    private void update() {
        switch (CaveGame.MENU_STATE) {
            case MAIN:
                currentMenu = mainMenu;
                break;
            case NEW_GAME:
                currentMenu = newGameMenu;
                break;
            case LOADING:
                drawString("Generating World...");
                CaveGame.APP_STATE = AppState.GAME;
                CaveGame.GAME_STATE = GameState.PLAY;
                break;
            case SAVING:
                drawString("Saving Game...");
                CaveGame.APP_STATE = AppState.MENU;
                CaveGame.MENU_STATE = MenuState.MAIN;
                break;
        }
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int mb) {
        screenX *= getWidth() / GameScreen.getWidth();
        screenY *= getHeight() / GameScreen.getHeight();
        for (ObjectMap.Entry<String, Button> entry : currentMenu.getButtons()) {
            Button button = entry.value;
            if (button.getRect().contains(screenX, screenY)) {
                if (button.getType() > 0) {
                    button.clicked();
                }
                break;
            }
        }
        return false;
    }

    @Override
    public void render() {
        update();
        spriter.begin();
        currentMenu.draw(spriter);
        drawString("CaveDroid " + CaveGame.VERSION, 0,
                getHeight() - getStringHeight("CaveDroid " + CaveGame.VERSION) * 1.5f);
        spriter.end();
    }
}
