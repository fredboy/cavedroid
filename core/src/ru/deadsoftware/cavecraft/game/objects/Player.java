package ru.deadsoftware.cavecraft.game.objects;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class Player implements Serializable {

    public static int ANIM_SPEED = 6;

    public Vector2 position;
    public Vector2 moveX, moveY;
    public int width, height, dir, texWidth;
    public boolean canJump;
    public int[] inventory;
    public boolean flyMode = false;

    public Player(Vector2 spawnPoint) {
        position = spawnPoint.cpy();
        moveX = new Vector2(0, 0);
        moveY = new Vector2(0, 0);
        width = 4;
        height = 30;
        texWidth = 8;
        inventory = new int[9];
    }

    public Rectangle getRect() {
        return new Rectangle(position.x + 2, position.y, width, height);
    }

}
