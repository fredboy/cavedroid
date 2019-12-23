package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import org.jetbrains.annotations.NotNull;
import ru.deadsoftware.cavedroid.misc.Assets;

import static ru.deadsoftware.cavedroid.GameScreen.GP;

/**
 * Falling sand is actually a mob, that spawns in place of gravel when there is no block under it,
 * falls down to the next block and becomes a block of sand again.
 */
public class FallingSand extends Mob {

    /**
     * Creates a FallingSand mob at coordinates
     * @param x X in pixels
     * @param y Y in pixels
     */
    public FallingSand(float x, float y) {
        super(x, y, 16, 16, Direction.LEFT, Type.SAND);
        move = new Vector2(0, 1);
    }

    @Override
    public void ai() {
        if (move.isZero()) {
            GP.world.setForeMap(getMapX(), getMiddleMapY(), 10);
            kill();
        }
    }

    @Override
    public void changeDir() {
    }

    @Override
    public void draw(@NotNull SpriteBatch spriteBatch, float x, float y) {
        spriteBatch.draw(Assets.sandSprite, x, y);
    }

}
