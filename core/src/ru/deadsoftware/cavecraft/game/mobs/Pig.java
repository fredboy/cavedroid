package ru.deadsoftware.cavecraft.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.misc.Assets;

public class Pig extends Mob {

    public Pig(int x, int y) {
        dir = MathUtils.random(1);
        position = new Vector2(x, y);
        moveX = new Vector2(-1 + dir * 2, 0);
        moveY = new Vector2(0, 0);
        width = 25;
        height = 18;
        canJump = false;
        dead = false;
    }

    @Override
    public void changeDir() {
        dir = -dir + 1;
        moveX.set(-1 + 2 * dir, 0);
    }

    @Override
    public void ai() {
        if (MathUtils.randomBoolean(.0025f)) changeDir();
        else if (MathUtils.randomBoolean(.0025f)) {
            if (moveX.x != 0f) moveX.setZero();
            else moveX.set(-1 + 2 * dir, 0);
        }
        if (moveX.x != 0f) animation += ANIM_SPEED;
        else animation = 0;
        if (animation >= 60 || animation <= -60) {
            ANIM_SPEED = -ANIM_SPEED;
        }
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y) {
        Assets.pigSprite[0][1].setRotation(animation);
        Assets.pigSprite[1][1].setRotation(-animation);
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
        return new Rectangle(position.x, position.y, width, height);
    }

    @Override
    public int getType() {
        return 0;
    }

}
