package ru.deadsoftware.cavecraft.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public abstract class Mob implements Serializable {

    protected int anim, animSpeed = 6;
    private float width, height;
    private int dir;

    public Vector2 pos;
    public Vector2 mov;
    private boolean dead;

    public boolean canJump;

    protected Mob(float x, float y, float width, float height, int dir) {
        pos = new Vector2(x, y);
        this.width = width;
        this.height = height;
        canJump = false;
        dead = false;
        this.dir = dir;
    }

    public int getMapX() {
        return (int) (pos.x + (getWidth() / 2)) / 16;
    }

    public int getMapY() {
        return (int) (pos.y + (getHeight() / 2)) / 16;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public int getDir() {
        return dir;
    }

    protected void switchDir() {
        dir = -dir + 1;
    }

    public boolean isDead() {
        return dead;
    }

    public void kill() {
        dead = true;
    }

    public Rectangle getRect() {
        return new Rectangle(pos.x, pos.y, getWidth(), getHeight());
    }

    public abstract void ai();

    public abstract void changeDir();

    public abstract void draw(SpriteBatch spriteBatch, float x, float y);

    public abstract int getType(); //0 - mob, 10 - sand, 11 - gravel
}
