package ru.deadsoftware.cavecraft.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.game.Items;
import ru.deadsoftware.cavecraft.misc.Assets;

public class FallingGravel extends Mob {

    public FallingGravel(int x, int y) {
        dir = 0;
        position = new Vector2(x, y);
        moveX = new Vector2(0, 0);
        moveY = new Vector2(0, 1);
        width = 16;
        height = 16;
        canJump = false;
        dead = false;
    }

    @Override
    public void ai() {
    }

    @Override
    public void changeDir() {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y) {
        spriteBatch.draw(Assets.blockTextures[Items.BLOCKS.get("gravel").getTexture()], x, y);
    }

    @Override
    public Rectangle getRect() {
        return new Rectangle(position.x, position.y, width, height);
    }

    @Override
    public int getType() {
        return 11;
    }

}
