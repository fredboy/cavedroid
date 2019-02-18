package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.misc.Assets;

public class Pig extends Mob {

    public Pig(float x, float y) {
        super(x, y, 25, 18, MathUtils.random(1));
        mov = new Vector2(-1 + getDir() * 2, 0);
    }

    @Override
    public void changeDir() {
        switchDir();
        mov.x = -1 + 2 * getDir();
    }

    @Override
    public void ai() {
        if (MathUtils.randomBoolean(.0025f)) changeDir();
        else if (MathUtils.randomBoolean(.0025f)) {
            if (mov.x != 0f) mov.x = 0;
            else mov.x = -1 + 2 * getDir();
        }
        if (mov.x != 0f) anim += animDelta;
        else anim = 0;
        if (anim >= 60 || anim <= -60) {
            animDelta = -animDelta;
        }
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y) {
        Assets.pigSprite[0][1].setRotation(anim);
        Assets.pigSprite[1][1].setRotation(-anim);
        //back legs
        Assets.pigSprite[1][1].setPosition(x - 4 + (9 - getDir() * 9), y + 6);
        Assets.pigSprite[1][1].draw(spriteBatch);
        Assets.pigSprite[1][1].setPosition(x + 17 - (9 * getDir()), y + 6);
        Assets.pigSprite[1][1].draw(spriteBatch);
        //front legs
        Assets.pigSprite[0][1].setPosition(x - 4 + (9 - getDir() * 9), y + 6);
        Assets.pigSprite[0][1].draw(spriteBatch);
        Assets.pigSprite[0][1].setPosition(x + 17 - (9 * getDir()), y + 6);
        Assets.pigSprite[0][1].draw(spriteBatch);
        //head & body
        spriteBatch.draw(Assets.pigSprite[getDir()][0], x, y);
    }

    @Override
    public int getType() {
        return 0;
    }

}
