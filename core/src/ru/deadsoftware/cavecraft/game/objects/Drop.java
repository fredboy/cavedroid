package ru.deadsoftware.cavecraft.game.objects;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class Drop implements Serializable {
    private int id;
    public Vector2 move, position;

    public static void pickUpDrop(Player pl, int id) {
        for (int i = 0; i < pl.inventory.length; i++) {
            if (pl.inventory[i] == 0) {
                pl.inventory[i] = id;
                break;
            }
        }
    }

    public Drop(float x, float y, int id) {
        this.id = id;
        position = new Vector2(x, y);
        move = new Vector2(0, -1);
    }

    public int getId() {
        return id;
    }

    public Rectangle getRect() {
        return new Rectangle(position.x, position.y, 8, 8);
    }

}
