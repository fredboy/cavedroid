package ru.deadsoftware.cavecraft.game.objects;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player {

    public Vector2 position;
    public Vector2 moveX, moveY;
    public int width, height, dir;
    public boolean canJump;
    public int[] inventory;
    public boolean flyMode = false;

    public Player() {
        position = new Vector2(0, 0);
        moveX = new Vector2(0, 0);
        moveY = new Vector2(0, 0);
        width = 4;
        height = 30;
        inventory = new int[9];
        inventory[0] = 1;
        inventory[1] = 2;
        inventory[2] = 3;
    }

    public Rectangle getRect() {
        return new Rectangle(position.x+2, position.y, width, height);
    }

}
