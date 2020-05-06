package ru.deadsoftware.cavedroid.game.objects;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.game.GameWorld;
import ru.deadsoftware.cavedroid.game.mobs.Player;

import java.io.Serializable;

public class Drop extends Rectangle implements Serializable {

    private final int id;
    private final Vector2 move;
    private boolean pickedUp = false;

    Drop(float x, float y, int id) {
        super(x, y, 8, 8);
        this.id = id;
        this.move = new Vector2(0, -1);
    }

    public Vector2 getMove() {
        return move;
    }

    public int closeToPlayer(GameWorld gameWorld, Player player) {
        boolean[] c = new boolean[3];

        c[0] = Intersector.overlaps(new Rectangle(player.getX() - 16,
                player.getY() - 16, player.getWidth() + 32, player.getHeight() + 32), this);
        c[1] = Intersector.overlaps(new Rectangle((player.getX() + gameWorld.getWidthPx()) - 16,
                player.getY() - 16, player.getWidth() + 32, player.getHeight() + 32), this);
        c[2] = Intersector.overlaps(new Rectangle((player.getX() - gameWorld.getWidthPx()) - 16,
                player.getY() - 16, player.getWidth() + 32, player.getHeight() + 32), this);

        for (int i = 0; i < 3; i++) {
            if (c[i]) {
                return i + 1;
            }
        }

        return 0;
    }

    public void moveToPlayer(GameWorld gameWorld, Player player, int ctp) {
        if (ctp > 0) {
            float px = player.getX();
            float py = player.getY();

            switch (ctp) {
                case 2:
                    px += gameWorld.getWidthPx();
                    break;
                case 3:
                    px -= gameWorld.getWidthPx();
                    break;
            }

            float dx = 0, dy = 0;

            if (px + player.getWidth() < x + 4) {
                dx = -.5f;
            } else if (px > x + 4) {
                dx = .5f;
            }

            if (py + player.getHeight() < y + 4) {
                dy = -.5f;
            } else if (py > y + 4) {
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

    private void checkWorldBounds() {
//        if (x + 8 > world.getWidthPx()) {
//            x -= world.getWidthPx();
//        } else if (x < 0) {
//            x += world.getWidthPx();
//        }
    }

    public void move() {
        x += move.x;
        y += move.y;
        checkWorldBounds();
        y = MathUtils.round(y);
    }

    public int getId() {
        return id;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }
}
