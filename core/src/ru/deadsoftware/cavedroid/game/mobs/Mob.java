package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.game.GameWorld;

import java.io.Serializable;

/**
 * Mob class.
 */
public abstract class Mob extends Rectangle implements Serializable {

    public enum Type {
        MOB,
        SAND,
        GRAVEL
    }

    public enum Direction {
        LEFT,
        RIGHT
    }

    protected Vector2 mVelocity;
    protected Type mType;
    protected int mAnimDelta = 6;
    protected int mAnim;

    private Direction mDirection;
    private boolean mDead;
    private boolean mCanJump;
    private boolean mFlyMode;

    /**
     * @param x          in pixels
     * @param y          in pixels
     * @param width      in pixels
     * @param height     in pixels
     * @param mDirection Direction in which mob is looking
     */
    protected Mob(float x, float y, float width, float height, Direction mDirection, Type type) {
        super(x, y, width, height);
        mVelocity = new Vector2(0, 0);
        mCanJump = false;
        mDead = false;
        this.mDirection = mDirection;
        this.mType = type;
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
        return mDirection;
    }

    public final boolean looksLeft() {
        return mDirection == Direction.LEFT;
    }

    public final boolean looksRight() {
        return mDirection == Direction.RIGHT;
    }

    /**
     * Switches direction in which mob is looking
     */
    protected final void switchDir() {
        mDirection = looksLeft() ? Direction.RIGHT : Direction.LEFT;
    }

    protected final int dirMultiplier() {
        return looksLeft() ? 0 : 1;
    }

    public final boolean isDead() {
        return mDead;
    }

    public final int getAnim() {
        return mAnim;
    }

    /**
     * Set's mob's dead variable to true and nothing else. It doesn't delete the
     */
    public final void kill() {
        mDead = true;
    }

    public final void move(float delta) {
        x += mVelocity.x * delta;
        y += mVelocity.y * delta;
    }

    public final Vector2 getVelocity() {
        return mVelocity;
    }

    public final boolean canJump() {
        return mCanJump;
    }

    public final void setCanJump(boolean canJump) {
        this.mCanJump = canJump;
    }

    public final boolean isFlyMode() {
        return mFlyMode;
    }

    public final void setFlyMode(boolean flyMode) {
        this.mFlyMode = flyMode;
    }

    public final Type getType() {
        return mType;
    }

    public void checkWorldBounds(GameWorld gameWorld) {
        if (x + width / 2 < 0) {
            x += gameWorld.getWidthPx();
        }
        if (x + width / 2 > gameWorld.getWidthPx()) {
            x -= gameWorld.getWidthPx();
        }
    }

    public abstract void draw(SpriteBatch spriteBatch, float x, float y);

    public abstract void ai(GameWorld gameWorld);

    public abstract void changeDir();
}
