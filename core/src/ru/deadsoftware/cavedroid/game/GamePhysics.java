package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.game.mobs.Mob;
import ru.deadsoftware.cavedroid.game.objects.Drop;
import ru.deadsoftware.cavedroid.game.objects.Player;

import java.util.Iterator;

import static ru.deadsoftware.cavedroid.GameScreen.GP;

class GamePhysics {

    static final int PL_SPEED = 2;

    private Vector2 gravity  = new Vector2(0, .9f);;

    private boolean checkJump(Rectangle rect, int dir) {
        int bl;
        int blX = (int) (rect.x + rect.width * dir - 8 + 16 * dir);
        int blY = (int) (rect.y + rect.height - 8);

        bl = GP.world.getForeMap(blX / 16, blY / 16);
        if (checkColl(new Rectangle(blX, rect.y - 18, rect.width, rect.height))) bl = 0;

        return (bl > 0 && GameItems.getBlock(bl).toJump() &&
                (rect.y + rect.height) - GameItems.getBlock(bl).getRect(blX / 16, blY / 16).y > 8);
    }

    private boolean checkColl(Rectangle rect) {
        int bl;
        int minX = (int) ((rect.x + rect.width / 2) / 16) - 4;
        int minY = (int) ((rect.y + rect.height / 2) / 16) - 4;
        int maxX = (int) ((rect.x + rect.width / 2) / 16) + 4;
        int maxY = (int) ((rect.y + rect.height / 2) / 16) + 4;
        if (minY < 0) minY = 0;
        if (maxY > GP.world.getHeight()) maxY = GP.world.getHeight();
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                bl = GP.world.getForeMap(x, y);
                if (bl > 0 && GameItems.getBlock(bl).hasCollision()) {
                    if (Intersector.overlaps(rect, GameItems.getBlock(bl).getRect(x, y))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int getBlock(Rectangle rect) {
        return GP.world.getForeMap((int) (rect.x + rect.width / 2) / 16, (int) (rect.y + rect.height / 8 * 7) / 16);
    }

    private void dropPhy(Drop drop) {
        if (drop.closeToPlayer() > 0) {
            drop.moveToPlayer();
        } else {
            if (drop.move.x >= .5f) drop.move.x -= .5f;
            else if (drop.move.x <= -.5f) drop.move.x += .5f;
            else drop.move.x = 0;
            if (drop.move.y < 9) drop.move.y += gravity.y / 4;
        }
        drop.pos.add(drop.move);
        if (drop.pos.x + 8 > GP.world.getWidthPx()) drop.pos.x -= GP.world.getWidthPx();
        else if (drop.pos.x < 0) drop.pos.x += GP.world.getWidthPx();
        drop.pos.y = MathUtils.round(drop.pos.y);
        while (checkColl(drop.getRect())) {
            drop.pos.y--;
            drop.move.y = 0;
        }
    }

    private void mobXColl(Mob mob) {
        if (checkColl(mob.getRect())) {
            if (mob.canJump && !mob.flyMode) {
                mob.pos.y -= 8;
            }
            if (checkColl(mob.getRect())) {
                if (mob.canJump && !mob.flyMode) mob.pos.y += 8;
                int d = 0;
                if (mob.mov.x < 0) d = 1;
                else if (mob.mov.x > 0) d = -1;
                mob.pos.x = MathUtils.round(mob.pos.x);
                while (checkColl(mob.getRect())) mob.pos.x += d;
                if (mob.canJump) mob.changeDir();
            }
        }
        if (mob.pos.x + mob.getWidth() / 2 < 0) mob.pos.x += GP.world.getWidthPx();
        if (mob.pos.x + mob.getWidth() / 2 > GP.world.getWidthPx()) mob.pos.x -= GP.world.getWidthPx();
    }

    private void mobYColl(Mob mob) {
        if (checkColl(mob.getRect())) {
            int d = -1;
            if (mob.mov.y < 0) d = 1;
            if (d == -1) {
                mob.canJump = true;
                mob.flyMode = false;
            }
            mob.pos.y = MathUtils.round(mob.pos.y);
            while (checkColl(mob.getRect())) mob.pos.y += d;
            mob.mov.y = 0;
            if (mob.getType() > 0) {
                GP.world.setForeMap(mob.getMapX(), mob.getMapY(), mob.getType());
                mob.kill();
            }
        } else {
            mob.canJump = false;
        }
        if (mob.pos.y > GP.world.getHeightPx()) {
            mob.kill();
        }
    }

    private void playerPhy(Player pl) {
        pl.pos.y += pl.mov.y;
        mobYColl(pl);
        if (pl.isDead()) return;

        if (GameItems.isFluid(getBlock(pl.getRect()))) {
            if (CaveGame.TOUCH && pl.mov.x != 0 && !pl.swim && !pl.flyMode) pl.swim = true;
            if (!pl.swim) {
                if (!pl.flyMode && pl.mov.y < 4.5f) pl.mov.add(gravity.x / 4, gravity.y / 4);
                if (!pl.flyMode && pl.mov.y > 4.5f) pl.mov.add(0, -1f);
            } else {
                pl.mov.add(0, -.5f);
                if (pl.mov.y < -3) pl.mov.y = -3;
            }
        } else {
            if (!pl.flyMode && pl.mov.y < 18) pl.mov.add(gravity);
        }

        pl.pos.x += pl.mov.x * (pl.flyMode ? 1.5f : 1) * (GameItems.isFluid(getBlock(pl.getRect())) && !pl.flyMode ? .8f : 1);
        mobXColl(pl);

        if (CaveGame.TOUCH && checkJump(pl.getRect(), pl.getDir()) && !pl.flyMode && pl.canJump && pl.mov.x != 0) {
            pl.mov.add(0, -8);
            pl.canJump = false;
        }
    }

    private void mobPhy(Mob mob) {
        mob.pos.y += mob.mov.y;
        mobYColl(mob);
        if (mob.isDead()) return;

        if (mob.getType() == 0 && GameItems.isFluid(getBlock(mob.getRect()))) {
            if (mob.mov.y > 9) mob.mov.add(0, -.9f);
            mob.mov.add(0, -.5f);
            if (mob.mov.y < -3) mob.mov.y = -3;
        } else if (!mob.flyMode && mob.mov.y < 18) mob.mov.add(gravity);

        mob.pos.x += mob.mov.x;
        mobXColl(mob);

        if (checkJump(mob.getRect(), mob.getDir()) && mob.canJump && mob.mov.x != 0) {
            mob.mov.add(0, -8);
            mob.canJump = false;
        }
    }

    void update(float delta) {
        //TODO use delta time
        for (Iterator<Drop> it = GP.drops.iterator(); it.hasNext(); ) {
            Drop drop = it.next();
            dropPhy(drop);
            if (Intersector.overlaps(drop.getRect(), GP.player.getRect())) drop.pickUpDrop(GP.player);
            if (drop.pickedUp) it.remove();
        }

        for (Iterator<Mob> it = GP.mobs.iterator(); it.hasNext(); ) {
            Mob mob = it.next();
            mob.ai();
            mobPhy(mob);
            if (mob.isDead()) it.remove();
        }

        playerPhy(GP.player);
        if (GP.player.isDead()) GP.player.respawn();

        GP.renderer.setCamPos(
                GP.player.pos.x + GP.player.getWidth() / 2 - GP.renderer.getWidth() / 2,
                GP.player.pos.y + GP.player.getHeight() / 2 - GP.renderer.getHeight() / 2);
    }

}
