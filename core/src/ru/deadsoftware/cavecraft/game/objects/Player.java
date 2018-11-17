package ru.deadsoftware.cavecraft.game.objects;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.game.GameItems;
import ru.deadsoftware.cavecraft.game.GameWorld;

import java.io.Serializable;

public class Player implements Serializable {

    public static int ANIM_SPEED = 6;

    public Vector2 pos;
    public Vector2 mov;
    private int width, height, dir, hp;
    public boolean canJump;
    public int[] inv;
    public boolean flyMode = false;
    public int gameMode;

    public Player(GameWorld world, int gameMode) {
        this.gameMode = gameMode;
        mov = new Vector2(0, 0);
        width = 4;
        height = 30;
        inv = new int[9];
        hp = 20;
        pos = getSpawnPoint(world).cpy();
    }

    public void respawn(GameWorld world) {
        pos.set(getSpawnPoint(world));
        mov.setZero();
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
            if (world.getForeMap(x, y) > 0 && GameItems.getBlock(world.getForeMap(x, y)).hasCollision()) break;
        }
        return new Vector2(x * 16 + 8 - (float) getWidth() / 2, (float) y * 16 - getHeight());
    }

    public int getMapX() {
        return (int) (pos.x + (getWidth() / 2)) / 16;
    }

    public int getMapY() {
        return (int) (pos.y + (getHeight() / 2)) / 16;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getDir() {
        return dir;
    }

    public void setDir(int dir) {
        this.dir = dir;
    }

    public Rectangle getRect() {
        return new Rectangle(pos.x, pos.y, getWidth(), getHeight());
    }

}
