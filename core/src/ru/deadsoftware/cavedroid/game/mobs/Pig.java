package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.game.GamePhysics;
import ru.deadsoftware.cavedroid.game.world.GameWorld;
import ru.deadsoftware.cavedroid.misc.Assets;
import ru.deadsoftware.cavedroid.misc.utils.SpriteUtilsKt;

import static ru.deadsoftware.cavedroid.misc.Assets.pigSprite;

public class Pig extends Mob {

    public Pig(float x, float y) {
        super(x, y, 25, 18, randomDir(), Type.MOB);
        mVelocity = new Vector2((looksLeft() ? -1 : 1) * GamePhysics.PL_SPEED, 0);
    }

    @Override
    public void changeDir() {
        switchDir();
        mVelocity.x = (-1 + 2 * dirMultiplier()) * GamePhysics.PL_SPEED;
    }

    @Override
    public void ai(GameWorld gameWorld, float delta) {
        if (MathUtils.randomBoolean(delta)) {
            if (mVelocity.x != 0f) {
                mVelocity.x = 0;
            } else {
                changeDir();
            }
        }
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y, float delta) {
        updateAnimation(delta);

        final Sprite frontLeg = pigSprite[0][1];
        final Sprite backLeg = pigSprite[1][1];
        final Sprite body = pigSprite[dirMultiplier()][0];

        SpriteUtilsKt.draw(spriteBatch, backLeg, x + (9 - dirMultiplier() * 9), y + 12, -mAnim);
        SpriteUtilsKt.draw(spriteBatch, backLeg, x + 21 - (9 * dirMultiplier()), y + 12, -mAnim);
        SpriteUtilsKt.draw(spriteBatch, body, x, y);
        SpriteUtilsKt.draw(spriteBatch, frontLeg, x + (9 - dirMultiplier() * 9), y + 12, mAnim);
        SpriteUtilsKt.draw(spriteBatch, frontLeg, x + 21 - (9 * dirMultiplier()), y + 12, mAnim);
    }
}
