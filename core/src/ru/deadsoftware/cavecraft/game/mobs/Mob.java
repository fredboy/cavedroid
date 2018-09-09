package ru.deadsoftware.cavecraft.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.misc.Assets;

import java.io.Serializable;

public abstract class Mob implements Serializable{

    public int ANIM_SPEED = 6;
    public Vector2 position;
    public Vector2 moveX, moveY;
    public int width, height, dir, animation;
    public boolean canJump;
    public boolean agressive;

    public abstract void ai();
    public abstract void changeDir();
    public abstract void draw(SpriteBatch spriteBatch, float x, float y);
    public abstract Rectangle getRect();

}
