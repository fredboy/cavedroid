package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.game.world.GameWorld;
import ru.deadsoftware.cavedroid.misc.Assets;

import static ru.deadsoftware.cavedroid.misc.Assets.pigSprite;

public class Pig extends Mob {

    public Pig(float x, float y) {
        super(x, y, 25, 18, randomDir(), Type.MOB);
        mVelocity = new Vector2(looksLeft() ? -1 : 1, 0);
    }

    @Override
    public void changeDir() {
        switchDir();
        mVelocity.x = -1 + 2 * dirMultiplier();
    }

    @Override
    public void ai(GameWorld gameWorld, float delta) {
        if (MathUtils.randomBoolean(.0025f)) {
            if (mVelocity.x != 0f) {
                mVelocity.x = 0;
            } else {
                changeDir();
            }
        }

        if (mVelocity.x != 0f) {
            mAnim += mAnimDelta * delta;
        } else {
            mAnim = 0;
        }

        if (mAnim >= 60 || mAnim <= -60) {
            mAnimDelta = -mAnimDelta;
        }
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y, float delta) {
        pigSprite[0][1].setRotation(getAnim());
        pigSprite[1][1].setRotation(-getAnim());
        //back legs
        pigSprite[1][1].setPosition(x + (9 - dirMultiplier() * 9), y + 12);
        pigSprite[1][1].draw(spriteBatch);
        pigSprite[1][1].setPosition(x + 21 - (9 * dirMultiplier()), y + 12);
        pigSprite[1][1].draw(spriteBatch);
        //head & body
        spriteBatch.draw(Assets.pigSprite[dirMultiplier()][0], x, y);
        //front legs
        pigSprite[0][1].setPosition(x + (9 - dirMultiplier() * 9), y + 12);
        pigSprite[0][1].draw(spriteBatch);
        pigSprite[0][1].setPosition(x + 21 - (9 * dirMultiplier()), y + 12);
        pigSprite[0][1].draw(spriteBatch);
    }
}
