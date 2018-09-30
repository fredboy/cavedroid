package ru.deadsoftware.cavecraft.game.objects;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class Player implements Serializable {

    public static int ANIM_SPEED = 6;

    public Vector2 pos;
    public Vector2 move;
    public int width, height, dir, texWidth;
    public boolean canJump;
    public int[] inv;
    public boolean flyMode = false;
    public int gameMode;

    public Player(Vector2 spawnPoint, int gameMode) {
        this.gameMode = gameMode;
        pos = spawnPoint.cpy();
        move = new Vector2(0, 0);
        width = 4;
        height = 30;
        texWidth = 8;
        inv = new int[9];
    }

    public Rectangle getRect() {
        return new Rectangle(pos.x + 2, pos.y, width, height);
    }

}
