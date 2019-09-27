package ru.deadsoftware.cavedroid.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import org.jetbrains.annotations.NotNull;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.GameScreen;
import ru.deadsoftware.cavedroid.game.GameItems;
import ru.deadsoftware.cavedroid.game.GameProc;
import ru.deadsoftware.cavedroid.game.GameSaver;
import ru.deadsoftware.cavedroid.menu.objects.Button;
import ru.deadsoftware.cavedroid.misc.InputHandlerGame;
import ru.deadsoftware.cavedroid.misc.states.AppState;
import ru.deadsoftware.cavedroid.misc.Assets;
import ru.deadsoftware.cavedroid.misc.Renderer;
import ru.deadsoftware.cavedroid.misc.states.GameState;
import ru.deadsoftware.cavedroid.misc.states.MenuState;

import static ru.deadsoftware.cavedroid.GameScreen.GP;
import static ru.deadsoftware.cavedroid.GameScreen.RENDERER;

public class MenuRenderer extends Renderer {

    public Array<Button> menuMainBtns;
    public Array<Button> menuNGBtns;

    public MenuRenderer(int width) {
        super(width, width * ((float) GameScreen.getHeight() / GameScreen.getWidth()));
        //main menu
        menuMainBtns = new Array<Button>();
        menuMainBtns.add(new Button("New game", getWidth() / 2 - 100, getHeight() / 4));
        menuMainBtns.add(new Button("Load game", getWidth() / 2 - 100, getHeight() / 4 + 30, GameSaver.exists() ? 1 : 0));
        menuMainBtns.add(new Button("Quit", getWidth() / 2 - 100, getHeight() / 4 + 60));
        //new game menu
        menuNGBtns = new Array<Button>();
        menuNGBtns.add(new Button("Survival", getWidth() / 2 - 100, getHeight() / 4, 0));
        menuNGBtns.add(new Button("Creative", getWidth() / 2 - 100, getHeight() / 4 + 30));
        menuNGBtns.add(new Button("Back", getWidth() / 2 - 100, getHeight() / 4 + 60));

    }

    private void newGame(int gameMode) {
        GP = new GameProc(gameMode);
        GP.player.respawn();
        GameSaver.save(GP);
        RENDERER = GP.renderer;
        Gdx.input.setInputProcessor(new InputHandlerGame());
        CaveGame.APP_STATE = AppState.GAME;
        CaveGame.GAME_STATE = GameState.PLAY;
    }

    public void buttonClicked(@NotNull Button button) {
        switch (button.getLabel().toLowerCase()) {
            case "new game":
                CaveGame.MENU_STATE = MenuState.NEW_GAME;
                break;
            case "load game":
                CaveGame.APP_STATE = AppState.LOAD;
                break;
            case "quit":
                Gdx.app.exit();
                break;
            case "survival":
                newGame(0);
                break;
            case "creative":
                newGame(1);
                break;
            case "back":
                CaveGame.MENU_STATE = MenuState.MAIN;
                break;
        }
    }

    private void drawButton(Button button) {
        spriter.draw(Assets.menuBtn[button.getType()], button.getX(), button.getY());
        setFontColor(255, 255, 255);
        drawString(button.getLabel(),
                (button.getX() + button.getWidth() / 2) - (float) Assets.getStringWidth(button.getLabel()) / 2,
                (button.getY() + button.getHeight() / 2) - (float) Assets.getStringHeight(button.getLabel()) / 2);
    }

    private void drawButtons(Array<Button> buttons) {
        for (Button button : buttons) {
            if (button.getType() > 0) {
                if (button.getRect().contains(Gdx.input.getX() * getWidth() / GameScreen.getWidth(),
                        Gdx.input.getY() * getHeight() / GameScreen.getHeight()) &&
                        (!CaveGame.TOUCH || Gdx.input.isTouched()))
                    button.setType(2);
                else button.setType(1);
            }
            drawButton(button);
        }
    }

    private void drawLabel(String str) {
        drawString(str);
    }

    @Override
    public void render() {
        spriter.begin();
        for (int x = 0; x <= getWidth() / 16; x++) {
            for (int y = 0; y <= getHeight() / 16; y++) {
                spriter.draw(GameItems.getBlock(3).getTex(), x * 16, y * 16);
                spriter.draw(Assets.shade, x * 16, y * 16);
            }
        }
        spriter.draw(Assets.gameLogo, getWidth() / 2 - (float) Assets.gameLogo.getWidth() / 2, 8);

        switch (CaveGame.MENU_STATE) {
            case MAIN:
                drawButtons(menuMainBtns);
                break;
            case NEW_GAME:
                drawButtons(menuNGBtns);
                break;
            case LOADING:
                drawLabel("Generating World...");
                CaveGame.APP_STATE = AppState.GAME;
                CaveGame.GAME_STATE = GameState.PLAY;
                break;
            case SAVING:
                drawLabel("Saving Game...");
                CaveGame.APP_STATE = AppState.MENU;
                CaveGame.MENU_STATE = MenuState.MAIN;
                break;
        }

        drawString("CaveDroid " + CaveGame.VERSION, 0,
                getHeight() - Assets.getStringHeight("CaveDroid " + CaveGame.VERSION) * 1.5f);
        spriter.end();
    }
}
