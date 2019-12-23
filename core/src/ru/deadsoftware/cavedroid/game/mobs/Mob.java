package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

import static ru.deadsoftware.cavedroid.GameScreen.GP;

/**
 * Mob class.
 */
public abstract class Mob extends Rectangle implements Serializable {

    protected Vector2 move;
    protected Type type;
    protected int animDelta = 6;
    protected int anim;
    private Direction dir;
    private boolean dead;
    private boolean canJump, flyMode;
    /**
     * @param x      in pixels
     * @param y      in pixels
     * @param width  in pixels
     * @param height in pixels
     * @param dir    Direction in which mob is looking
     */
    protected Mob(float x, float y, float width, float height, Direction dir, Type type) {
        super(x, y, width, height);
        move = new Vector2(0, 0);
        canJump = false;
        dead = false;
        this.dir = dir;
        this.type = type;
    }

    protected static Direction randomDir() {
        return MathUtils.randomBoolean(.5f) ? Direction.LEFT : Direction.RIGHT;
    }

    /**
     * @return The X coordinate of a mob in blocks
     */
    public final int getMapX() {
        return (int) (x + (getWidth() / 2)) / 16;
    }

    /**
     * @return The Y coordinate of mob's upper edge in blocks
     */
    public final int getUpperMapY() {
        return (int) (y / 16);
    }

    /**
     * @return The Y coordinate if mob's vertical center in blocks
     */
    public final int getMiddleMapY() {
        return (int) (y + (getHeight() / 2)) / 16;
    }

    /**
     * @return The Y coordinate of mob's legs in blocks
     */
    public final int getLowerMapY() {
        return (int) (y + getHeight()) / 16;
    }

    public final float getWidth() {
        return width;
    }

    public final float getHeight() {
        return height;
    }

    /**
     * @return Integer representing a direction in which mob is looking, where 0 is left and 1 is right
     */
    public final Direction getDirection() {
        return dir;
    }

    public final boolean looksLeft() {
        return dir == Direction.LEFT;
    }

    public final boolean looksRight() {
        return dir == Direction.RIGHT;
    }

    /**
     * Switches direction in which mob is looking
     */
    protected final void switchDir() {
        dir = looksLeft() ? Direction.RIGHT : Direction.LEFT;
    }

    protected final int dirMultiplier() {
        return looksLeft() ? 0 : 1;
    }

    public final boolean isDead() {
        return dead;
    }

    public final int getAnim() {
        return anim;
    }

    /**
     * Set's mob's dead variable to true and nothing else. It doesn't delete the
     */
    public final void kill() {
        dead = true;
    }

    public final void move() {
        x += move.x;
        y += move.y;
    }

    public final Vector2 getMove() {
        return move;
    }

    public final boolean canJump() {
        return canJump;
    }

    public final void setCanJump(boolean canJump) {
        this.canJump = canJump;
    }

    public final boolean isFlyMode() {
        return flyMode;
    }

    public final void setFlyMode(boolean flyMode) {
        this.flyMode = flyMode;
    }

    public final Type getType() {
        return type;
    }

    public void checkWorldBounds() {
        if (x + width / 2 < 0) {
            x += GP.world.getWidthPx();
        }
        if (x + width / 2 > GP.world.getWidthPx()) {
            x -= GP.world.getWidthPx();
        }
    }

    public abstract void draw(SpriteBatch spriteBatch, float x, float y);

    public abstract void ai();

    public abstract void changeDir();

    public enum Type {
        MOB,
        SAND,
        GRAVEL
    }

    public enum Direction {
        LEFT,
        RIGHT
    }
}
