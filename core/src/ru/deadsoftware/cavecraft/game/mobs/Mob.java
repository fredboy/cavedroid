package ru.deadsoftware.cavecraft.game.mobs;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class Mob {

    public Vector2 position;
    public Vector2 moveX, moveY;
    public int width, height, dir;
    public boolean canJump;

    public abstract void ai();
    public abstract Rectangle getRect();

}
