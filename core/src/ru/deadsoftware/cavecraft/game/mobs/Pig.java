package ru.deadsoftware.cavecraft.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.misc.Assets;

public class Pig extends Mob {

    public Pig(int x, int y) {
        dir = MathUtils.random(1);
        pos = new Vector2(x, y);
        move = new Vector2(-1 + dir * 2, 0);
        width = 25;
        height = 18;
        canJump = false;
        dead = false;
    }

    @Override
    public void changeDir() {
        dir = -dir + 1;
        move.x = -1 + 2 * dir;
    }

    @Override
    public void ai() {
        if (MathUtils.randomBoolean(.0025f)) changeDir();
        else if (MathUtils.randomBoolean(.0025f)) {
            if (move.x != 0f) move.x = 0;
            else move.x = -1 + 2 * dir;
        }
        if (move.x != 0f) anim += ANIM_SPEED;
        else anim = 0;
        if (anim >= 60 || anim <= -60) {
            ANIM_SPEED = -ANIM_SPEED;
        }
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y) {
        Assets.pigSprite[0][1].setRotation(anim);
        Assets.pigSprite[1][1].setRotation(-anim);
        //back legs
        Assets.pigSprite[1][1].setPosition(x - 4 + (9 - dir * 9), y + 6);
        Assets.pigSprite[1][1].draw(spriteBatch);
        Assets.pigSprite[1][1].setPosition(x + 17 - (9 * dir), y + 6);
        Assets.pigSprite[1][1].draw(spriteBatch);
        //front legs
        Assets.pigSprite[0][1].setPosition(x - 4 + (9 - dir * 9), y + 6);
        Assets.pigSprite[0][1].draw(spriteBatch);
        Assets.pigSprite[0][1].setPosition(x + 17 - (9 * dir), y + 6);
        Assets.pigSprite[0][1].draw(spriteBatch);
        //head & body
        spriteBatch.draw(Assets.pigSprite[dir][0], x, y);
    }

    @Override
    public Rectangle getRect() {
        return new Rectangle(pos.x, pos.y, width, height);
    }

    @Override
    public int getType() {
        return 0;
    }

}
