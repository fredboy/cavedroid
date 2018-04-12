package ru.deadsoftware.cavecraft.game.objects;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.Assets;

public class Player {

    public Vector2 position;
    public Vector2 moveX, moveY;
    public int width, height, dir, texWidth;
    public boolean canJump;
    public int[] inventory;
    public boolean flyMode = false;

    public Player() {
        position = new Vector2(0, 0);
        moveX = new Vector2(0, 0);
        moveY = new Vector2(0, 0);
        width = 4;
        height = 30;
        texWidth = 8;
        inventory = new int[9];
        inventory[0] = 1;
        inventory[1] = 2;
        inventory[2] = 3;
    }

    public Rectangle getRect() {
        return new Rectangle(position.x+2, position.y, width, height);
    }

}
