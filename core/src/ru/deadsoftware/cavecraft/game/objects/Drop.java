package ru.deadsoftware.cavecraft.game.objects;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.game.GameProc;

import java.io.Serializable;

public class Drop implements Serializable {
    private int id;
    public boolean pickedUp = false;
    public Vector2 move, pos;

    public Drop(float x, float y, int id) {
        this.id = id;
        pos = new Vector2(x, y);
        move = new Vector2(0, -1);
    }

    public int closeToPlayer(GameProc gp) {
        boolean c1 = Intersector.overlaps(new Rectangle(gp.player.pos.x - 16, gp.player.pos.y - 16, gp.player.texWidth + 32, gp.player.height + 32), getRect());
        boolean c2 = Intersector.overlaps(new Rectangle((gp.player.pos.x + gp.world.getWidth() * 16) - 16, gp.player.pos.y - 16, gp.player.texWidth + 32, gp.player.height + 32), getRect());
        boolean c3 = Intersector.overlaps(new Rectangle((gp.player.pos.x - gp.world.getWidth() * 16) - 16, gp.player.pos.y - 16, gp.player.texWidth + 32, gp.player.height + 32), getRect());
        if (c1) return 1;
        if (c2) return 2;
        if (c3) return 3;
        return 0;
    }

    public void moveToPlayer(GameProc gp) {
        int ctp = closeToPlayer(gp);
        if (ctp > 0) {
            float px = gp.player.pos.x;
            float py = gp.player.pos.y;
            switch (ctp) {
                case 2:
                    px += gp.world.getWidth() * 16;
                    break;
                case 3:
                    px -= gp.world.getWidth() * 16;
                    break;
            }
            float dx = 0, dy = 0;
            if (px + gp.player.texWidth < pos.x + 4) dx = -.5f;
            else if (px > pos.x + 4) dx = .5f;
            if (py + gp.player.height < pos.y + 4) dy = -.5f;
            else if (py > pos.y + 4) dy = .5f;
            move.add(dx, dy);
            if (move.x > 2) move.x = 1;
            if (move.x < -2) move.x = -1;
            if (move.y > 2) move.y = 1;
            if (move.y < -2) move.y = -1;
        }
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
        return new Rectangle(pos.x, pos.y, 8, 8);
    }

}
