package ru.deadsoftware.cavecraft.game.objects;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.game.GameItems;
import ru.deadsoftware.cavecraft.game.GameWorld;

import java.io.Serializable;

public class Player implements Serializable {

    public static int ANIM_SPEED = 6;

    public Vector2 pos;
    public Vector2 move;
    public int width, height, dir, texWidth, hp;
    public boolean canJump;
    public int[] inv;
    public boolean flyMode = false;
    public int gameMode;

    public Player(GameWorld world, int gameMode) {
        this.gameMode = gameMode;
        pos = getSpawnPoint(world).cpy();
        move = new Vector2(0, 0);
        width = 4;
        height = 30;
        texWidth = 8;
        inv = new int[9];
        hp = 20;
    }

    public void respawn(GameWorld world) {
        pos.set(getSpawnPoint(world));
        move.setZero();
        hp = 20;
    }

    private Vector2 getSpawnPoint(GameWorld world) {
        int x = 0, y;
        for (y = 0; y < world.getHeight(); y++) {
            if (y == world.getHeight() - 1) {
                y = 60;
                world.setForeMap(x, y, 1);
                break;
            }
            if (world.getForeMap(x, y) > 0 && GameItems.getBlock(world.getForeMap(x, y)).coll) break;
        }
        x = x * 16 + texWidth / 2;
        y = y * 16 - height;
        return new Vector2(x, y);
    }

    public Rectangle getRect() {
        return new Rectangle(pos.x + 2, pos.y, width, height);
    }

}
