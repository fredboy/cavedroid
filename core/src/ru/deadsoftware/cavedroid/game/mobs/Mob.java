package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

/**
 * Mob class.
 */
public abstract class Mob implements Serializable {

    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    private final float width;
    private final float height;
    private int dir;

    public boolean flyMode;
    public final Vector2 pos;
    public Vector2 mov;

    private boolean dead;

    protected int animDelta = 6;
    public boolean canJump;
    int anim;

    /**
     *
     * @param x in pixels
     * @param y in pixels
     * @param width in pixels
     * @param height in pixels
     * @param dir integer representing a direction where 0 is left and 1 is right.
     *            You should use {@link #LEFT} and {@link #RIGHT} constants
     */
    protected Mob(float x, float y, float width, float height, int dir) {
        pos = new Vector2(x, y);
        mov = new Vector2(0, 0);
        this.width = width;
        this.height = height;
        canJump = false;
        flyMode = false;
        dead = false;
        this.dir = dir;
    }

    /**
     *
     * @return The X coordinate of a mob in blocks
     */
    public int getMapX() {
        return (int) (pos.x + (getWidth() / 2)) / 16;
    }

    /**
     *
     * @return The Y coordinate of mob's upper edge in blocks
     */
    public int getUpperMapY() {
        return (int) (pos.y / 16);
    }

    /**
     *
     * @return The Y coordinate if mob's vertical center in blocks
     */
    public int getMiddleMapY() {
        return (int) (pos.y + (getHeight() / 2)) / 16;
    }

    /**
     *
     * @return The Y coordinate of mob's legs in blocks
     */
    public int getLowerMapY() {
        return (int) (pos.y + getHeight()) / 16;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    /**
     *
     * @return Integer representing a direction in which mob is looking, where 0 is left and 1 is right
     */
    public int getDirection() {
        return dir;
    }

    public boolean looksLeft() {
        return getDirection() == LEFT;
    }

    public boolean looksRight() {
        return getDirection() == RIGHT;
    }

    /**
     * Switches direction in which mob is looking
     */
    protected void switchDir() {
        dir = looksLeft() ? RIGHT : LEFT;
    }

    public boolean isDead() {
        return dead;
    }

    /**
     * Set's mob's dead variable to true and nothing else. It doesn't delete the mob.
     */
    public void kill() {
        dead = true;
    }

    /**
     *
     * @return A {@link Rectangle} with mob's coordinates and size
     */
    public Rectangle getRect() {
        return new Rectangle(pos.x, pos.y, getWidth(), getHeight());
    }

    public abstract void ai();

    public abstract void changeDir();

    public abstract void draw(SpriteBatch spriteBatch, float x, float y);

    /**
     *
     * @return 0 - if regular mob. <br>
     *     10 - if instance of {@link FallingSand} <br> 11 - if instance of {@link FallingGravel}
     */
    public abstract int getType(); //0 - mob, 10 - sand, 11 - gravel
}
