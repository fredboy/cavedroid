package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.game.mobs.Mob;
import ru.deadsoftware.cavedroid.game.mobs.Player;
import ru.deadsoftware.cavedroid.game.objects.Drop;

import java.util.Iterator;

import static ru.deadsoftware.cavedroid.GameScreen.GP;

class GamePhysics {

    static final int PL_SPEED = 2;

    private final Vector2 gravity = new Vector2(0, .9f);

    /**
     * Checks if mob should jump
     *
     * @return true if mob should jump
     */
    private boolean checkJump(Mob mob) {
        int dir = mob.looksLeft() ? 0 : 1;
        int blX = (int) (mob.getX() + mob.getWidth() * dir - 8 + 16 * dir);
        int blY = (int) (mob.getY() + mob.getHeight() - 8);
        int block = GP.world.getForeMap(blX / 16, blY / 16);

        if (checkColl(new Rectangle(blX, mob.getY() - 18, mob.getWidth(), mob.getHeight()))) {
            block = 0;
        }

        return (block > 0 && GameItems.getBlock(block).toJump() &&
                (mob.getY() + mob.getHeight()) - GameItems.getBlock(block).getRect(blX / 16, blY / 16).y > 8);
    }

    private boolean checkColl(Rectangle rect) {
        int minX = (int) ((rect.x + rect.width / 2) / 16) - 4;
        int minY = (int) ((rect.y + rect.height / 2) / 16) - 4;
        int maxX = (int) ((rect.x + rect.width / 2) / 16) + 4;
        int maxY = (int) ((rect.y + rect.height / 2) / 16) + 4;

        if (minY < 0) {
            minY = 0;
        }

        if (maxY > GP.world.getHeight()) {
            maxY = GP.world.getHeight();
        }

        int block;
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                block = GP.world.getForeMap(x, y);
                if (block > 0 && GameItems.getBlock(block).hasCollision()) {
                    if (Intersector.overlaps(rect, GameItems.getBlock(block).getRect(x, y))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private int getBlock(Rectangle rect) {
        return GP.world.getForeMap((int) (rect.x + rect.width / 2) / 16,
                (int) (rect.y + rect.height / 8 * 7) / 16);
    }

    private void dropPhy(Drop drop) {
        if (drop.closeToPlayer() > 0) {
            drop.moveToPlayer();
        } else {
            if (drop.getMove().x >= .5f) {
                drop.getMove().x -= .5f;
            } else if (drop.getMove().x <= -.5f) {
                drop.getMove().x += .5f;
            } else {
                drop.getMove().x = 0;
            }
            if (drop.getMove().y < 9) {
                drop.getMove().y += gravity.y / 4;
            }
        }
        drop.move();


        if (checkColl(drop)) {
            drop.getMove().set(0, -1);
            do {
                drop.move();
            } while (checkColl(drop));
            drop.getMove().setZero();
        }
    }

    private void mobXColl(Mob mob) {
        if (checkColl(mob)) {
            if (mob.canJump() && !mob.isFlyMode()) {
                mob.y -= 8;
            }

            if (checkColl(mob)) {
                if (mob.canJump() && !mob.isFlyMode()) {
                    mob.y += 8;
                }

                int d = 0;

                if (mob.getMove().x < 0) {
                    d = 1;
                } else if (mob.getMove().x > 0) {
                    d = -1;
                }

                mob.x = MathUtils.round(mob.getX());

                while (checkColl(mob)) {
                    mob.x += d;
                }

                if (mob.canJump()) {
                    mob.changeDir();
                }
            }
        }

        mob.checkWorldBounds();
    }

    private void mobYColl(Mob mob) {
        if (checkColl(mob)) {
            int d = -1;

            if (mob.getMove().y < 0) {
                d = 1;
            }

            if (d == -1) {
                mob.setCanJump(true);
                mob.setFlyMode(false);
            }

            mob.y = MathUtils.round(mob.getY());

            while (checkColl(mob)) {
                mob.y += d;
            }

            mob.getMove().y = 0;

        } else {
            mob.setCanJump(false);
        }

        if (mob.getY() > GP.world.getHeightPx()) {
            mob.kill();
        }
    }

    private void playerPhy(Player player) {
        player.y += player.getMove().y;
        mobYColl(player);

        if (player.isDead()) {
            return;
        }

        if (GameItems.isFluid(getBlock(player))) {
            if (CaveGame.TOUCH && player.getMove().x != 0 && !player.swim && !player.isFlyMode()) {
                player.swim = true;
            }
            if (!player.swim) {
                if (!player.isFlyMode() && player.getMove().y < 4.5f) {
                    player.getMove().add(gravity.x / 4, gravity.y / 4);
                }
                if (!player.isFlyMode() && player.getMove().y > 4.5f) {
                    player.getMove().add(0, -1f);
                }
            } else {
                player.getMove().add(0, -.5f);
                if (player.getMove().y < -3) {
                    player.getMove().y = -3;
                }
            }
        } else {
            if (!player.isFlyMode() && player.getMove().y < 18) {
                player.getMove().add(gravity);
            }
        }

        player.x += player.getMove().x * (player.isFlyMode() ? 1.5f : 1) *
                (GameItems.isFluid(getBlock(player)) && !player.isFlyMode() ? .8f : 1);

        mobXColl(player);

        if (CaveGame.TOUCH && !player.isFlyMode() && player.canJump() && player.getMove().x != 0 && checkJump(player)) {
            player.getMove().add(0, -8);
            player.setCanJump(false);
        }
    }

    private void mobPhy(Mob mob) {
        if (mob.getType() == Mob.Type.MOB && GameItems.isFluid(getBlock(mob))) {
            if (mob.getMove().y > 9) {
                mob.getMove().add(0, -.9f);
            }

            mob.getMove().add(0, -.5f);

            if (mob.getMove().y < -3) {
                mob.getMove().y = -3;
            }
        } else if (!mob.isFlyMode() && mob.getMove().y < 18) {
            mob.getMove().add(gravity);
        }

        mob.y += mob.getMove().y;
        mobYColl(mob);

        if (mob.isDead()) {
            return;
        }

        mob.x += mob.getMove().x;
        mobXColl(mob);

        if (mob.canJump() && mob.getMove().x != 0 && checkJump(mob)) {
            mob.getMove().add(0, -8);
            mob.setCanJump(false);
        }
    }

    void update() {
        for (Iterator<Drop> it = GP.drops.iterator(); it.hasNext(); ) {
            Drop drop = it.next();
            dropPhy(drop);
            if (Intersector.overlaps(drop, GP.player)) {
                drop.pickUpDrop(GP.player);
            }
            if (drop.isPickedUp()) {
                it.remove();
            }
        }

        for (Iterator<Mob> it = GP.mobs.iterator(); it.hasNext(); ) {
            Mob mob = it.next();
            mob.ai();
            mobPhy(mob);
            if (mob.isDead()) {
                it.remove();
            }
        }

        playerPhy(GP.player);
        if (GP.player.isDead()) {
            GP.player.respawn();
        }

        GP.renderer.setCamPos(GP.player.getX() + GP.player.getWidth() / 2 - GP.renderer.getWidth() / 2,
                GP.player.getY() + GP.player.getHeight() / 2 - GP.renderer.getHeight() / 2);
    }

}
