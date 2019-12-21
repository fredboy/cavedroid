package ru.deadsoftware.cavedroid.game.objects;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.game.mobs.Player;

import java.io.Serializable;

import static ru.deadsoftware.cavedroid.GameScreen.GP;

public class Drop implements Serializable {

    private final int id;
    public boolean pickedUp = false;
    public final Vector2 move;
    public final Vector2 pos;

    public Drop(float x, float y, int id) {
        this.id = id;
        pos = new Vector2(x, y);
        move = new Vector2(0, -1);
    }

    public int closeToPlayer() {
        boolean[] c = new boolean[3];

        c[0] = Intersector.overlaps(new Rectangle(GP.player.getX() - 16,
                        GP.player.getY() - 16, GP.player.getWidth() + 32, GP.player.getHeight() + 32),
                getRect());
        c[1] = Intersector.overlaps(new Rectangle((GP.player.getX() + GP.world.getWidthPx()) - 16,
                        GP.player.getY() - 16, GP.player.getWidth() + 32, GP.player.getHeight() + 32),
                getRect());
        c[2] = Intersector.overlaps(new Rectangle((GP.player.getX() - GP.world.getWidthPx()) - 16,
                        GP.player.getY() - 16, GP.player.getWidth() + 32, GP.player.getHeight() + 32),
                getRect());

        for (int i = 0; i < 3; i++) {
            if (c[i]) {
                return i + 1;
            }
        }
        return 0;
    }

    public void moveToPlayer() {
        int ctp = closeToPlayer();
        if (ctp > 0) {
            float px = GP.player.getX();
            float py = GP.player.getY();
            switch (ctp) {
                case 2:
                    px += GP.world.getWidthPx();
                    break;
                case 3:
                    px -= GP.world.getWidthPx();
                    break;
            }
            float dx = 0, dy = 0;
            if (px + GP.player.getWidth() < pos.x + 4) {
                dx = -.5f;
            } else if (px > pos.x + 4) {
                dx = .5f;
            }

            if (py + GP.player.getHeight() < pos.y + 4) {
                dy = -.5f;
            } else if (py > pos.y + 4) {
                dy = .5f;
            }

            move.add(dx, dy);

            if (move.x > 2) {
                move.x = 1;
            } else if (move.x < -2) {
                move.x = -1;
            }

            if (move.y > 2) {
                move.y = 1;
            } else if (move.y < -2) {
                move.y = -1;
            }
        }
    }

    public void pickUpDrop(Player pl) {
        for (int i = 0; i < pl.inventory.length; i++) {
            if (pl.inventory[i] == 0 || pl.inventory[i] == id) {
                pl.inventory[i] = id;
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
