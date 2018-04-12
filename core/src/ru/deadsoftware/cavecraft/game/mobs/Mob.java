package ru.deadsoftware.cavecraft.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class Mob {

    public static int ANIM_SPEED = 6;

    public Vector2 position;
    public Vector2 moveX, moveY;
    public int width, height, dir, animation, anim_d;
    public boolean canJump;

    public abstract void ai();
    public abstract void draw(SpriteBatch spriteBatch, float x, float y);
    public abstract Rectangle getRect();

}
