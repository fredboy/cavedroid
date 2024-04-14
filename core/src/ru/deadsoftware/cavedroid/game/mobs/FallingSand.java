package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.game.GameItems;
import ru.deadsoftware.cavedroid.game.GameWorld;


/**
 * Falling sand is actually a mob, that spawns in place of gravel when there is no block under it,
 * falls down to the next block and becomes a block of sand again.
 */
public class FallingSand extends Mob {

    /**
     * Creates a FallingSand mob at coordinates
     *
     * @param x X in pixels
     * @param y Y in pixels
     */
    public FallingSand(float x, float y) {
        super(x, y, 16, 16, Direction.LEFT, Type.SAND);
        mVelocity = new Vector2(0, 1);
    }

    @Override
    public void ai(GameWorld gameWorld, float delta) {
        if (mVelocity.isZero()) {
            gameWorld.setForeMap(getMapX(), getMiddleMapY(), 10);
            kill();
        }
    }

    @Override
    public void changeDir() {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y, float delta) {
        spriteBatch.draw(GameItems.getBlockTex(10), x, y);
    }

}
