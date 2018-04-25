package ru.deadsoftware.cavecraft.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.misc.Assets;

import java.io.Serializable;

public abstract class Mob implements Serializable{

    public static int ANIM_SPEED = 6;
    public static int ANIMATION = 0;
    public Vector2 position;
    public Vector2 moveX, moveY;
    public int width, height, dir;
    public boolean canJump;
    public boolean agressive;

    public static void animateMobs() {
        Assets.pigSprite[0][1].setRotation(ANIMATION);
        Assets.pigSprite[1][1].setRotation(-ANIMATION);
        ANIMATION+=ANIM_SPEED;
        if (ANIMATION>=60 || ANIMATION<=-60) {
            ANIM_SPEED = -ANIM_SPEED;
        }
    }

    public abstract void ai();
    public abstract void draw(SpriteBatch spriteBatch, float x, float y);
    public abstract Rectangle getRect();

}
