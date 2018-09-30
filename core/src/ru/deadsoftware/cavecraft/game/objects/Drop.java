package ru.deadsoftware.cavecraft.game.objects;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class Drop implements Serializable {
    private int id;
    public boolean pickedUp = false;
    public Vector2 move, position;

    public Drop(float x, float y, int id) {
        this.id = id;
        position = new Vector2(x, y);
        move = new Vector2(0, -1);
    }

    public void pickUpDrop(Player pl) {
        for (int i = 0; i < pl.inv.length; i++) {
            if (pl.inv[i] == 0 || pl.inv[i] == id) {
                pl.inv[i] = id;
                pickedUp = true;
                break;
            }
        }
    }

    public int getId() {
        return id;
    }

    public Rectangle getRect() {
        return new Rectangle(position.x, position.y, 8, 8);
    }

}
