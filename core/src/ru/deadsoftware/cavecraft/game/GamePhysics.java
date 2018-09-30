package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.CaveGame;
import ru.deadsoftware.cavecraft.game.mobs.Mob;
import ru.deadsoftware.cavecraft.game.objects.Drop;
import ru.deadsoftware.cavecraft.game.objects.Player;

import java.util.Iterator;

public class GamePhysics {

    public static final int PL_SPEED = 2;

    private GameProc gameProc;

    private Vector2 gravity;

    public GamePhysics(GameProc gameProc) {
        this.gameProc = gameProc;
        gravity = new Vector2(0, .9f);
    }

    private boolean checkJump(Rectangle rect, int dir) {
        int bl;
        switch (dir) {
            case 0:
                bl = gameProc.world.getForeMap((int) ((rect.x - 8) / 16), (int) ((rect.y + rect.height - 8) / 16));
                if (checkColl(new Rectangle(rect.x - 8, rect.y - 18, rect.width, rect.height))) bl = 0;
                break;
            case 1:
                bl = gameProc.world.getForeMap((int) ((rect.x + rect.width + 8) / 16), (int) ((rect.y + rect.height - 8) / 16));
                if (checkColl(new Rectangle(rect.x + rect.width + 8, rect.y - 18, rect.width, rect.height))) bl = 0;
                break;
            default:
                bl = 0;
        }
        return (bl > 0 && Items.BLOCKS.getValueAt(bl).toJump() &&
                (rect.y + rect.height) - Items.BLOCKS.getValueAt(bl).getRect((int) ((rect.x - 8) / 16), (int) ((rect.y + rect.height - 8) / 16)).y > 8);
    }

    private boolean checkColl(Rectangle rect) {
        int bl;
        int minX = (int) ((rect.x + rect.width / 2) / 16) - 4;
        int minY = (int) ((rect.y + rect.height / 2) / 16) - 4;
        int maxX = (int) ((rect.x + rect.width / 2) / 16) + 4;
        int maxY = (int) ((rect.y + rect.height / 2) / 16) + 4;
        if (minY < 0) minY = 0;
        if (maxY > gameProc.world.getHeight()) maxY = gameProc.world.getHeight();
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                bl = gameProc.world.getForeMap(x, y);
                if (bl > 0 && Items.BLOCKS.getValueAt(bl).collision) {
                    if (Intersector.overlaps(rect, Items.BLOCKS.getValueAt(bl).getRect(x, y))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int getBlock(Rectangle rect) {
        return gameProc.world.getForeMap((int) (rect.x + rect.width / 2) / 16, (int) (rect.y + rect.height / 8 * 7) / 16);
    }

    private void dropPhy(Drop drop) {
        if (drop.move.y < 9) drop.move.y += gravity.y / 4;
        drop.position.add(drop.move);
        drop.position.y = MathUtils.round(drop.position.y);
        while (checkColl(drop.getRect())) {
            drop.position.y--;
            drop.move.y = 0;
        }
    }

    private void playerPhy(Player pl) {
        pl.position.add(pl.moveY);
        if (checkColl(pl.getRect())) {
            int d = -1;
            if (pl.moveY.y < 0) d = 1;
            if (d == -1) {
                pl.flyMode = false;
                pl.canJump = true;
            }
            pl.position.y = MathUtils.round(pl.position.y);
            while (checkColl(pl.getRect())) pl.position.y += d;
            pl.moveY.setZero();
        } else {
            pl.canJump = false;
        }

        if (Items.isFluid(getBlock(pl.getRect()))) {
            if (CaveGame.TOUCH && pl.moveX.x != 0 && !gameProc.swim && !pl.flyMode) gameProc.swim = true;
            if (!gameProc.swim) {
                if (!pl.flyMode && pl.moveY.y < 4.5f) pl.moveY.add(gravity.x / 4, gravity.y / 4);
                if (!pl.flyMode && pl.moveY.y > 4.5f) pl.moveY.add(0, -1f);
            } else {
                pl.moveY.add(0, -.5f);
                if (pl.moveY.y < -3) pl.moveY.y = -3;
            }
        } else {
            if (!pl.flyMode && pl.moveY.y < 18) pl.moveY.add(gravity);
        }

        pl.position.add(pl.moveX);
        if (checkColl(pl.getRect())) {
            if (pl.canJump && !pl.flyMode) pl.position.y -= 8;
            if (checkColl(pl.getRect())) {
                if (pl.canJump && !pl.flyMode) pl.position.y += 8;
                int d = 0;
                if (pl.moveX.x < 0) d = 1;
                else if (pl.moveX.x > 0) d = -1;
                pl.position.x = MathUtils.round(pl.position.x);
                while (checkColl(pl.getRect())) pl.position.x += d;
            }
        }
        if (pl.position.x + pl.texWidth / 2 < 0) pl.position.x += gameProc.world.getWidth() * 16;
        if (pl.position.x + pl.texWidth / 2 > gameProc.world.getWidth() * 16)
            pl.position.x -= gameProc.world.getWidth() * 16;
        if (pl.position.y > gameProc.world.getHeight() * 16) {
            pl.position = gameProc.world.getSpawnPoint().cpy();
        }
        if (CaveGame.TOUCH && checkJump(pl.getRect(), pl.dir) && !pl.flyMode && pl.canJump && !pl.moveX.equals(Vector2.Zero)) {
            pl.moveY.add(0, -8);
            pl.canJump = false;
        }
    }

    private void mobPhy(Mob mob) {
        mob.position.add(mob.moveY);
        if (checkColl(mob.getRect())) {
            int d = -1;
            if (mob.moveY.y < 0) d = 1;
            if (d == -1) mob.canJump = true;
            mob.position.y = MathUtils.round(mob.position.y);
            while (checkColl(mob.getRect())) mob.position.y += d;
            mob.moveY.setZero();
            if (mob.getType() > 0) {
                gameProc.world.setForeMap((int) mob.position.x / 16, (int) mob.position.y / 16, mob.getType());
                mob.position.y = -1;
                mob.dead = true;
            }
        } else {
            mob.canJump = false;
        }

        if (mob.getType() == 0 && Items.isFluid(getBlock(mob.getRect()))) {
            if (mob.moveY.y > 9) mob.moveY.add(0, -.9f);
            mob.moveY.add(0, -.5f);
            if (mob.moveY.y < -3) mob.moveY.y = -3;
        } else if (mob.moveY.y < 18) mob.moveY.add(gravity);

        mob.position.add(mob.moveX);
        if (checkColl(mob.getRect())) {
            if (mob.canJump) {
                mob.position.y -= 8;
            }
            if (checkColl(mob.getRect())) {
                if (mob.canJump) mob.position.y += 8;
                int d = 0;
                if (mob.moveX.x < 0) d = 1;
                else if (mob.moveX.x > 0) d = -1;
                mob.position.x = MathUtils.round(mob.position.x);
                while (checkColl(mob.getRect())) mob.position.x += d;
                if (mob.canJump) mob.changeDir();
            }
        }
        if (mob.position.x + mob.width / 2 < 0) mob.position.x += gameProc.world.getWidth() * 16;
        if (mob.position.x + mob.width / 2 > gameProc.world.getWidth() * 16)
            mob.position.x -= gameProc.world.getWidth() * 16;
        if (mob.position.y > gameProc.world.getHeight() * 16) {
            mob.position.y = 0;
        }
        if (checkJump(mob.getRect(), mob.dir) && mob.canJump && !mob.moveX.equals(Vector2.Zero)) {
            mob.moveY.add(0, -8);
            mob.canJump = false;
        }
    }

    public void update(float delta) {
        for (Iterator<Drop> it = gameProc.drops.iterator(); it.hasNext(); ) {
            Drop drop = it.next();
            dropPhy(drop);
            if (Intersector.overlaps(drop.getRect(), gameProc.player.getRect()))
                drop.pickUpDrop(gameProc.player);
            if (drop.pickedUp) it.remove();
        }

        for (Iterator<Mob> it = gameProc.mobs.iterator(); it.hasNext(); ) {
            Mob mob = it.next();
            mob.ai();
            mobPhy(mob);
            if (mob.dead)
                it.remove();
        }

        playerPhy(gameProc.player);

        gameProc.renderer.camera.position.set(
                gameProc.player.position.x + gameProc.player.texWidth / 2 - gameProc.renderer.camera.viewportWidth / 2,
                gameProc.player.position.y + gameProc.player.height / 2 - gameProc.renderer.camera.viewportHeight / 2,
                0
        );
    }

}
