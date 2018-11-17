package ru.deadsoftware.cavecraft.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.game.GameItems;
import ru.deadsoftware.cavecraft.misc.Assets;

public class FallingGravel extends Mob {

    public FallingGravel(float x, float y) {
        super(x, y, 16, 16, 0);
        mov = new Vector2(0, 1);
    }

    @Override
    public void ai() {
    }

    @Override
    public void changeDir() {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y) {
        spriteBatch.draw(Assets.blockTex[GameItems.getBlock(11).getTex()], x, y);
    }

    @Override
    public int getType() {
        return 11;
    }

}
