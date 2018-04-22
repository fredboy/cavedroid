package ru.deadsoftware.cavecraft.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import ru.deadsoftware.cavecraft.*;
import ru.deadsoftware.cavecraft.menu.objects.Button;

public class MenuRenderer extends Renderer {

    public Array<Button> buttons;

    public MenuRenderer(int width) {
        super(width,width*((float) GameScreen.getHeight()/GameScreen.getWidth()));
        buttons = new Array<Button>();
        buttons.add(new Button("Play", getWidth()/2-100, getHeight()/4));
        buttons.add(new Button("Quit", getWidth()/2-100, getHeight()/4+30));
    }

    public void buttonClicked(Button button) {
        if (button.getLabel().toLowerCase().equals("play")) {
            CaveGame.STATE = GameState.RESTART;
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

    @Override
    public void render() {
        spriteBatch.begin();
        for (int x=0; x<=getWidth(); x++)
            for (int y=0; y<=getHeight(); y++)
                spriteBatch.draw(Items.BLOCKS.get("dirt").getTexture(),x*16,y*16);
        spriteBatch.draw(Assets.gameLogo, getWidth()/2-Assets.gameLogo.getWidth()/2, 0);
        for (Button button : buttons) {
            if (button.getRect().contains(Gdx.input.getX()*getWidth()/GameScreen.getWidth(),
                    Gdx.input.getY()*getHeight()/GameScreen.getHeight()) && button.getType()>0) {
                button.setType(2);
            } else {
                button.setType(1);
            }
            drawButton(button);
        }
        drawString("CaveCraft "+CaveGame.VERSION,0,
                getHeight()-Assets.getStringHeight("CaveCraft "+CaveGame.VERSION)*2);
        spriteBatch.end();
    }
}
