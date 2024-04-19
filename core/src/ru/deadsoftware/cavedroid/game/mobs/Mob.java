package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.game.world.GameWorld;

import java.io.Serializable;

/**
 * Mob class.
 */
public abstract class Mob extends Rectangle implements Serializable {

    protected static int ANIMATION_SPEED = 360;

    public enum Type {
        MOB,
        SAND,
        GRAVEL
    }

    public enum Direction {

        LEFT(0, -1),
        RIGHT(1, 1);

        private final int index;
        private final int basis;

        /**
         * Index for this direction (left = 0, right = 1)
         */
        public final int getIndex() {
            return index;
        }

        /**
         * Basis for this direction (left = -1, right = 1)
         */
        public final int getBasis() {
            return basis;
        }

        Direction(int index, int basis) {
            this.index = index;
            this.basis = basis;
        }
    }

    protected Vector2 mVelocity;
    protected Type mType;
    protected int mAnimDelta = ANIMATION_SPEED;
    protected float mAnim;

    private Direction mDirection;
    protected boolean mDead;
    private boolean mCanJump;
    private boolean mFlyMode;

    private final int mMaxHealth;
    private int mHealth;

    /**
     * @param x          in pixels
     * @param y          in pixels
     * @param width      in pixels
     * @param height     in pixels
     * @param mDirection Direction in which mob is looking
     */
    protected Mob(float x, float y, float width, float height, Direction mDirection, Type type, int maxHealth) {
        super(x, y, width, height);
        mVelocity = new Vector2(0, 0);
        mCanJump = false;
        mDead = false;
        this.mDirection = mDirection;
        this.mType = type;
        this.mMaxHealth = maxHealth;
        this.mHealth = mMaxHealth;
    }

    protected static Direction randomDir() {
        return MathUtils.randomBoolean(.5f) ? Direction.LEFT : Direction.RIGHT;
    }

    private boolean isAnimationIncreasing() {
        return mAnim > 0 && mAnimDelta > 0 || mAnim < 0 && mAnimDelta < 0;
    }

    private void checkHealth() {
        mHealth = MathUtils.clamp(mHealth, 0, mMaxHealth);

        if (mHealth <= 0) {
            kill();
        }
    }

    protected final void updateAnimation(float delta) {
        if (mVelocity.x != 0f || Math.abs(mAnim) > mAnimDelta * delta) {
            mAnim += mAnimDelta * delta;
        } else {
            mAnim = 0;
        }

        if (mAnim > 60f) {
            mAnim = 60f;
            mAnimDelta = -ANIMATION_SPEED;
        } else if (mAnim < -60f) {
            mAnim = -60f;
            mAnimDelta = ANIMATION_SPEED;
        }

        if (mVelocity.x == 0f && isAnimationIncreasing()) {
            mAnimDelta = -mAnimDelta;
        }
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

    public final boolean isDead() {
        return mDead;
    }

    public final float getAnim() {
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

    protected final void setVelocity(Vector2 velocity) {
        mVelocity = velocity;
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

    public final void checkWorldBounds(GameWorld gameWorld) {
        if (x + width / 2 < 0) {
            x += gameWorld.getWidthPx();
        }
        if (x + width / 2 > gameWorld.getWidthPx()) {
            x -= gameWorld.getWidthPx();
        }
    }

    public final int getHealth() {
        return mHealth;
    }

    public void damage(int damage) {
        if (damage < 0) {
            Gdx.app.error(this.getClass().getSimpleName(), "Damage cant be negative!");
            return;
        }

        if (mHealth <= Integer.MIN_VALUE + damage) {
            mHealth = Integer.MIN_VALUE + damage;
        }

        mHealth -= damage;
        checkHealth();
    }

    public void heal(int heal) {
        if (heal < 0) {
            Gdx.app.error(this.getClass().getSimpleName(), "Heal cant be negative!");
            return;
        }

        if (mHealth >= Integer.MAX_VALUE - heal) {
            mHealth = Integer.MAX_VALUE - heal;
        }

        mHealth += heal;
        checkHealth();
    }

    public abstract void draw(SpriteBatch spriteBatch, float x, float y, float delta);

    public abstract void ai(GameWorld gameWorld, float delta);

    public abstract void changeDir();

    public abstract float getSpeed();

    public abstract void jump();
}
