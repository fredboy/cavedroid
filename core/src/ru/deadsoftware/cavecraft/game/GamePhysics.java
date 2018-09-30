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

    private GameProc gp;

    private Vector2 gravity;

    public GamePhysics(GameProc gp) {
        this.gp = gp;
        gravity = new Vector2(0, .9f);
    }

    private boolean checkJump(Rectangle rect, int dir) {
        int bl;
        switch (dir) {
            case 0:
                bl = gp.world.getForeMap((int) ((rect.x - 8) / 16), (int) ((rect.y + rect.height - 8) / 16));
                if (checkColl(new Rectangle(rect.x - 8, rect.y - 18, rect.width, rect.height))) bl = 0;
                break;
            case 1:
                bl = gp.world.getForeMap((int) ((rect.x + rect.width + 8) / 16), (int) ((rect.y + rect.height - 8) / 16));
                if (checkColl(new Rectangle(rect.x + rect.width + 8, rect.y - 18, rect.width, rect.height))) bl = 0;
                break;
            default:
                bl = 0;
        }
        return (bl > 0 && GameItems.getBlock(bl).toJump() &&
                (rect.y + rect.height) - GameItems.getBlock(bl).getRect((int) ((rect.x - 8) / 16), (int) ((rect.y + rect.height - 8) / 16)).y > 8);
    }

    private boolean checkColl(Rectangle rect) {
        int bl;
        int minX = (int) ((rect.x + rect.width / 2) / 16) - 4;
        int minY = (int) ((rect.y + rect.height / 2) / 16) - 4;
        int maxX = (int) ((rect.x + rect.width / 2) / 16) + 4;
        int maxY = (int) ((rect.y + rect.height / 2) / 16) + 4;
        if (minY < 0) minY = 0;
        if (maxY > gp.world.getHeight()) maxY = gp.world.getHeight();
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                bl = gp.world.getForeMap(x, y);
                if (bl > 0 && GameItems.getBlock(bl).coll) {
                    if (Intersector.overlaps(rect, GameItems.getBlock(bl).getRect(x, y))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int getBlock(Rectangle rect) {
        return gp.world.getForeMap((int) (rect.x + rect.width / 2) / 16, (int) (rect.y + rect.height / 8 * 7) / 16);
    }

    private void dropPhy(Drop drop) {
        if (drop.move.y < 9) drop.move.y += gravity.y / 4;
        drop.pos.add(drop.move);
        drop.pos.y = MathUtils.round(drop.pos.y);
        while (checkColl(drop.getRect())) {
            drop.pos.y--;
            drop.move.y = 0;
        }
    }

    private void playerPhy(Player pl) {
        pl.pos.y += pl.move.y;
        if (checkColl(pl.getRect())) {
            int d = -1;
            if (pl.move.y < 0) d = 1;
            if (d == -1) {
                pl.flyMode = false;
                pl.canJump = true;
            }
            pl.pos.y = MathUtils.round(pl.pos.y);
            while (checkColl(pl.getRect())) pl.pos.y += d;
            pl.move.y = 0;
        } else {
            pl.canJump = false;
        }

        if (GameItems.isFluid(getBlock(pl.getRect()))) {
            if (CaveGame.TOUCH && pl.move.x != 0 && !gp.swim && !pl.flyMode) gp.swim = true;
            if (!gp.swim) {
                if (!pl.flyMode && pl.move.y < 4.5f) pl.move.add(gravity.x / 4, gravity.y / 4);
                if (!pl.flyMode && pl.move.y > 4.5f) pl.move.add(0, -1f);
            } else {
                pl.move.add(0, -.5f);
                if (pl.move.y < -3) pl.move.y = -3;
            }
        } else {
            if (!pl.flyMode && pl.move.y < 18) pl.move.add(gravity);
        }

        pl.pos.x += pl.move.x;
        if (checkColl(pl.getRect())) {
            if (pl.canJump && !pl.flyMode) pl.pos.y -= 8;
            if (checkColl(pl.getRect())) {
                if (pl.canJump && !pl.flyMode) pl.pos.y += 8;
                int d = 0;
                if (pl.move.x < 0) d = 1;
                else if (pl.move.x > 0) d = -1;
                pl.pos.x = MathUtils.round(pl.pos.x);
                while (checkColl(pl.getRect())) pl.pos.x += d;
            }
        }
        if (pl.pos.x + pl.texWidth / 2 < 0) pl.pos.x += gp.world.getWidth() * 16;
        if (pl.pos.x + pl.texWidth / 2 > gp.world.getWidth() * 16)
            pl.pos.x -= gp.world.getWidth() * 16;
        if (pl.pos.y > gp.world.getHeight() * 16) {
            pl.pos = gp.world.getSpawnPoint().cpy();
        }
        if (CaveGame.TOUCH && checkJump(pl.getRect(), pl.dir) && !pl.flyMode && pl.canJump && pl.move.x != 0) {
            pl.move.add(0, -8);
            pl.canJump = false;
        }
    }

    private void mobPhy(Mob mob) {
        mob.pos.y += mob.move.y;
        if (checkColl(mob.getRect())) {
            int d = -1;
            if (mob.move.y < 0) d = 1;
            if (d == -1) mob.canJump = true;
            mob.pos.y = MathUtils.round(mob.pos.y);
            while (checkColl(mob.getRect())) mob.pos.y += d;
            mob.move.y = 0;
            if (mob.getType() > 0) {
                gp.world.setForeMap((int) mob.pos.x / 16, (int) mob.pos.y / 16, mob.getType());
                mob.pos.y = -1;
                mob.dead = true;
            }
        } else {
            mob.canJump = false;
        }

        if (mob.getType() == 0 && GameItems.isFluid(getBlock(mob.getRect()))) {
            if (mob.move.y > 9) mob.move.add(0, -.9f);
            mob.move.add(0, -.5f);
            if (mob.move.y < -3) mob.move.y = -3;
        } else if (mob.move.y < 18) mob.move.add(gravity);

        mob.pos.x += mob.move.x;
        if (checkColl(mob.getRect())) {
            if (mob.canJump) {
                mob.pos.y -= 8;
            }
            if (checkColl(mob.getRect())) {
                if (mob.canJump) mob.pos.y += 8;
                int d = 0;
                if (mob.move.x < 0) d = 1;
                else if (mob.move.x > 0) d = -1;
                mob.pos.x = MathUtils.round(mob.pos.x);
                while (checkColl(mob.getRect())) mob.pos.x += d;
                if (mob.canJump) mob.changeDir();
            }
        }
        if (mob.pos.x + mob.width / 2 < 0) mob.pos.x += gp.world.getWidth() * 16;
        if (mob.pos.x + mob.width / 2 > gp.world.getWidth() * 16)
            mob.pos.x -= gp.world.getWidth() * 16;
        if (mob.pos.y > gp.world.getHeight() * 16) {
            mob.pos.y = 0;
        }
        if (checkJump(mob.getRect(), mob.dir) && mob.canJump && mob.move.x != 0) {
            mob.move.add(0, -8);
            mob.canJump = false;
        }
    }

    public void update(float delta) {
        for (Iterator<Drop> it = gp.drops.iterator(); it.hasNext(); ) {
            Drop drop = it.next();
            dropPhy(drop);
            if (Intersector.overlaps(drop.getRect(), gp.player.getRect()))
                drop.pickUpDrop(gp.player);
            if (drop.pickedUp) it.remove();
        }

        for (Iterator<Mob> it = gp.mobs.iterator(); it.hasNext(); ) {
            Mob mob = it.next();
            mob.ai();
            mobPhy(mob);
            if (mob.dead)
                it.remove();
        }

        playerPhy(gp.player);

        gp.renderer.setCamPos(
                gp.player.pos.x + gp.player.texWidth / 2 - gp.renderer.getWidth() / 2,
                gp.player.pos.y + gp.player.height / 2 - gp.renderer.getHeight() / 2);
    }

}
