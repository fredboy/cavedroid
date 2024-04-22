package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.game.GameItemsHolder;
import ru.deadsoftware.cavedroid.game.world.GameWorld;
import ru.deadsoftware.cavedroid.misc.Assets;

import javax.annotation.CheckForNull;


/**
 * Falling sand is actually a mob, that spawns in place of gravel when there is no block under it,
 * falls down to the next block and becomes a block of sand again.
 */
public class FallingSand extends Mob {

    private static final String TAG = "FallingSand";

    /**
     * Creates a FallingSand mob at coordinates
     *
     * @param x X in pixels
     * @param y Y in pixels
     */
    public FallingSand(float x, float y) {
        super(x, y, 16, 16, Direction.LEFT, Type.SAND, Integer.MAX_VALUE);
        mVelocity = new Vector2(0, 1);
    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public void jump() {
        // no-op
    }

    @Override
    public void ai(GameWorld gameWorld, GameItemsHolder gameItemsHolder, float delta) {
        if (mVelocity.isZero()) {
            gameWorld.setForeMap(getMapX(), getUpperMapY(), gameItemsHolder.getBlock("sand"));
            kill();
        }
    }

    @Override
    public void changeDir() {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y, float delta) {
        @CheckForNull final Texture texture = Assets.blockTextures.get("sand");

        if (texture == null) {
            Gdx.app.error(TAG, "Couldn't draw: texture not found");
            kill();
            return;
        }

        spriteBatch.draw(texture, x, y);
    }

}
