package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.misc.Assets;

import static ru.deadsoftware.cavedroid.GameScreen.GP;
import static ru.deadsoftware.cavedroid.misc.Assets.pigSprite;

public class Pig extends Mob {

    public Pig(float x, float y) {
        super(x, y, 25, 18, Mob.randomDir());
        mov = new Vector2(looksLeft() ? -1 : 1, 0);
    }

    @Override
    public void changeDir() {
        switchDir();
        mov.x = -1 + 2 * getDirection();
    }

    @Override
    public void ai() {
        if (MathUtils.randomBoolean(.0025f)) changeDir();
        else if (MathUtils.randomBoolean(.0025f)) {
            if (mov.x != 0f) mov.x = 0;
            else mov.x = -1 + 2 * getDirection();
        }
        if (mov.x != 0f) anim += animDelta;
        else anim = 0;
        if (anim >= 60 || anim <= -60) {
            animDelta = -animDelta;
        }
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y) {
        if (x + getWidth() - GP.world.getWidthPx() >= 0 && x - GP.world.getWidthPx() <= getWidth()) {
            x -= GP.world.getWidthPx();
        } else if (x + getWidth() + GP.world.getWidthPx() >= 0 && x + GP.world.getWidthPx() <= getWidth()) {
            x += GP.world.getWidthPx();
        }

        pigSprite[0][1].setRotation(getAnim());
        pigSprite[1][1].setRotation(-getAnim());
        //back legs
        pigSprite[1][1].setPosition(x - 4 + (9 - getDirection() * 9), y + 6);
        pigSprite[1][1].draw(spriteBatch);
        pigSprite[1][1].setPosition(x + 17 - (9 * getDirection()), y + 6);
        pigSprite[1][1].draw(spriteBatch);
        //front legs
        pigSprite[0][1].setPosition(x - 4 + (9 - getDirection() * 9), y + 6);
        pigSprite[0][1].draw(spriteBatch);
        pigSprite[0][1].setPosition(x + 17 - (9 * getDirection()), y + 6);
        pigSprite[0][1].draw(spriteBatch);
        //head & body
        spriteBatch.draw(Assets.pigSprite[getDirection()][0], x, y);
    }
}
