package ru.deadsoftware.cavecraft.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import ru.deadsoftware.cavecraft.*;
import ru.deadsoftware.cavecraft.game.GameSaver;
import ru.deadsoftware.cavecraft.menu.objects.Button;

public class MenuRenderer extends Renderer {

    public Array<Button> menuMainButtons;

    public MenuRenderer(int width) {
        super(width,width*((float) GameScreen.getHeight()/GameScreen.getWidth()));
        menuMainButtons = new Array<Button>();
        menuMainButtons.add(new Button("New game", getWidth()/2-100, getHeight()/4));
        menuMainButtons.add(new Button("Load game", getWidth()/2-100, getHeight()/4+30,GameSaver.exists()?1:0));
        menuMainButtons.add(new Button("Quit", getWidth()/2-100, getHeight()/4+60));
    }

    public void buttonClicked(Button button) {
        if (button.getLabel().toLowerCase().equals("new game")) {
            CaveGame.STATE = AppState.GOTO_NEW_GAME;
        } else if (button.getLabel().toLowerCase().equals("load game")) {
            CaveGame.STATE = AppState.GOTO_LOAD_GAME;
        } else if (button.getLabel().toLowerCase().equals("quit")) {
            Gdx.app.exit();
        }
    }

    private void drawButton(Button button) {
        spriteBatch.draw(Assets.menuButton[button.getType()], button.getX(), button.getY());
        setFontColor(255,255,255);
        drawString(button.getLabel(),
                (button.getX()+button.getWidth()/2)-Assets.getStringWidth(button.getLabel())/2,
                (button.getY()+button.getHeight()/2)-Assets.getStringHeight(button.getLabel())/2);
    }

    private void drawMenuMain() {
        for (Button button : menuMainButtons) {
            if (button.getType()>0) {
                if (button.getRect().contains(Gdx.input.getX()*getWidth()/GameScreen.getWidth(),
                        Gdx.input.getY()*getHeight()/GameScreen.getHeight()))
                    button.setType(2);
                else button.setType(1);
            }
            drawButton(button);
        }
    }

    public void drawLabel(String str) {
        drawString(str);
    }

    @Override
    public void render() {
        spriteBatch.begin();
        for (int x=0; x<=getWidth()/16; x++)
            for (int y=0; y<=getHeight()/16; y++) {
                spriteBatch.draw(Items.BLOCKS.get("dirt").getTexture(), x * 16, y * 16);
                spriteBatch.draw(Assets.shade,x*16,y*16);
            }
        spriteBatch.draw(Assets.gameLogo, getWidth()/2-Assets.gameLogo.getWidth()/2, 0);

        switch (CaveGame.STATE) {
            case MENU_MAIN: drawMenuMain(); break;
            case GOTO_NEW_GAME: case GOTO_LOAD_GAME: drawLabel("Generating World..."); break;
            case GOTO_MENU: drawLabel("Saving Game..."); break;
        }

        drawString("CaveCraft "+CaveGame.VERSION,0,
                getHeight()-Assets.getStringHeight("CaveCraft "+CaveGame.VERSION)*1.5f);
        spriteBatch.end();

        switch (CaveGame.STATE) {
            case GOTO_NEW_GAME: CaveGame.STATE = AppState.NEW_GAME; break;
            case GOTO_LOAD_GAME: CaveGame.STATE = AppState.LOAD_GAME; break;
            case GOTO_MENU: CaveGame.STATE = AppState.SAVE_GAME; break;
        }

    }
}
