package ru.deadsoftware.cavecraft.menu;

import ru.deadsoftware.cavecraft.Assets;
import ru.deadsoftware.cavecraft.GameScreen;
import ru.deadsoftware.cavecraft.Items;
import ru.deadsoftware.cavecraft.Renderer;

public class MenuRenderer extends Renderer {

    public MenuRenderer() {
        super(480,480*((float) GameScreen.getHeight()/GameScreen.getWidth()));
    }

    @Override
    public void render() {
        spriteBatch.begin();
        for (int x=0; x<=getWidth(); x++)
            for (int y=0; y<=getHeight(); y++)
                spriteBatch.draw(Items.BLOCKS.get("dirt").getTexture(),x*16,y*16);
        spriteBatch.draw(Assets.gameLogo, getWidth()/2-Assets.gameLogo.getWidth()/2, 0);
        spriteBatch.end();
    }
}
