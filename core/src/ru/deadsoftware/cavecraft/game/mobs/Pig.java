package ru.deadsoftware.cavecraft.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.game.GameWorld;
import ru.deadsoftware.cavecraft.misc.Assets;
import ru.deadsoftware.cavecraft.game.GameProc;

public class Pig extends Mob{

    public Pig(int x, int y) {
        position = new Vector2(x, y);
        moveX = new Vector2(0, 0);
        moveY = new Vector2(0, 0);
        width = 25;
        height = 18;
        dir = 0;
        canJump = false;
        agressive = false;
    }

    @Override
    public void ai() {
        if (MathUtils.randomBoolean(.0025f)) dir=-dir+1;
        moveX.set(-1+2*dir,0);
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y) {
        //back legs
        Assets.pigSprite[1][1].setPosition(x-4+(9-dir*9),y+6);
        Assets.pigSprite[1][1].draw(spriteBatch);
        Assets.pigSprite[1][1].setPosition(x+17-(9*dir),y+6);
        Assets.pigSprite[1][1].draw(spriteBatch);
        //front legs
        Assets.pigSprite[0][1].setPosition(x-4+(9-dir*9),y+6);
        Assets.pigSprite[0][1].draw(spriteBatch);
        Assets.pigSprite[0][1].setPosition(x+17-(9*dir),y+6);
        Assets.pigSprite[0][1].draw(spriteBatch);
        //head & body
        spriteBatch.draw(Assets.pigSprite[dir][0], x, y);
    }

    @Override
    public Rectangle getRect() {
        return new Rectangle(position.x, position.y, width, height);
    }
}
