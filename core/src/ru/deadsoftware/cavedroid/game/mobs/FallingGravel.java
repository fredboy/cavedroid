package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.game.GameItems;
import ru.deadsoftware.cavedroid.game.world.GameWorld;

/**
 * Falling gravel is actually a mob, that spawns in place of gravel when there is no block under it,
 * falls down to the next block and becomes a block of gravel again.
 */
public class FallingGravel extends Mob {

    /**
     * Creates a FallingGravel mob at coordinates
     *
     * @param x X in pixels
     * @param y Y in pixels
     */
    public FallingGravel(float x, float y) {
        super(x, y, 16, 16, Direction.LEFT, Type.GRAVEL);
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
    public void ai(GameWorld gameWorld, float delta) {
        if (mVelocity.isZero()) {
            gameWorld.setForeMap(getMapX(), getMiddleMapY(), 11);
            kill();
        }
    }

    @Override
    public void changeDir() {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y, float delta) {
        spriteBatch.draw(GameItems.getBlockTex(11), x, y);
    }

}
