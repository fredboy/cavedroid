package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.mobs.Mob;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.mobs.Player;
import ru.deadsoftware.cavedroid.game.objects.Drop;
import ru.deadsoftware.cavedroid.game.objects.DropController;
import ru.deadsoftware.cavedroid.game.world.GameWorld;

import javax.inject.Inject;
import java.util.Iterator;


@GameScope
public class GamePhysics {

    public static final float PL_SPEED = 69.072f;
    public static final float PL_JUMP_VELOCITY = -133.332f;
    public static final float PL_TERMINAL_VELOCITY = 1254.4f;

    private final Vector2 gravity = new Vector2(0, 444.44f);

    private final GameWorld mGameWorld;
    private final MainConfig mMainConfig;
    private final MobsController mMobsController;
    private final DropController mDropController;

    @Inject
    public GamePhysics(GameWorld gameWorld,
                       MainConfig mainConfig,
                       MobsController mobsController,
                       DropController dropController) {
        mGameWorld = gameWorld;
        mMainConfig = mainConfig;
        mMobsController = mobsController;
        mDropController = dropController;
    }

    /**
     * Checks if mob should jump
     *
     * @return true if mob should jump
     */
    private boolean checkJump(Mob mob) {
        int dir = mob.looksLeft() ? 0 : 1;
        int blX = (int) (mob.getX() + mob.getWidth() * dir - 8 + 16 * dir);
        int blY = (int) (mob.getY() + mob.getHeight() - 8);
        int block = mGameWorld.getForeMap(blX / 16, blY / 16);

        if (checkColl(new Rectangle(blX, mob.getY() - 18, mob.getWidth(), mob.getHeight()))) {
            block = 0;
        }

        return (block > 0 && GameItems.getBlock(block).toJump() &&
                (mob.getY() + mob.getHeight()) - GameItems.getBlock(block).getRectangle(blX / 16, blY / 16).y > 8);
    }

    private boolean checkColl(Rectangle rect) {
        int minX = (int) ((rect.x + rect.width / 2) / 16) - 4;
        int minY = (int) ((rect.y + rect.height / 2) / 16) - 4;
        int maxX = (int) ((rect.x + rect.width / 2) / 16) + 4;
        int maxY = (int) ((rect.y + rect.height / 2) / 16) + 4;

        if (minY < 0) {
            minY = 0;
        }

        if (maxY > mGameWorld.getHeight()) {
            maxY = mGameWorld.getHeight();
        }

        int block;
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                block = mGameWorld.getForeMap(x, y);
                if (block > 0 && GameItems.getBlock(block).hasCollision()) {
                    if (Intersector.overlaps(rect, GameItems.getBlock(block).getRectangle(x, y))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private int getBlock(Rectangle rect) {
        return mGameWorld.getForeMap((int) (rect.x + rect.width / 2) / 16,
                (int) (rect.y + rect.height / 8 * 7) / 16);
    }

    private void dropPhy(Drop drop, float delta) {
        int dropToPlayer = drop.closeToPlayer(mGameWorld, mMobsController.getPlayer());

        if (dropToPlayer > 0) {
            drop.moveToPlayer(mGameWorld, mMobsController.getPlayer(), dropToPlayer, delta);
        } else {
            if (drop.getVelocity().x >= 300f) {
                drop.getVelocity().x = 300f;
            } else if (drop.getVelocity().x <= -300f) {
                drop.getVelocity().x = -300f;
            } else {
                drop.getVelocity().x = 0;
            }
            if (drop.getVelocity().y < PL_TERMINAL_VELOCITY) {
                drop.getVelocity().y += gravity.y * delta;
            }
        }
        drop.move(delta);


        if (checkColl(drop)) {
            drop.getVelocity().set(0, -1);
            do {
                drop.move(1);
            } while (checkColl(drop));
            drop.getVelocity().setZero();
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

                if (mob.getVelocity().x < 0) {
                    d = 1;
                } else if (mob.getVelocity().x > 0) {
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

        mob.checkWorldBounds(mGameWorld);
    }

    private void mobYColl(Mob mob) {
        if (checkColl(mob)) {
            int d = -1;

            if (mob.getVelocity().y < 0) {
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

            mob.getVelocity().y = 0;



            //todo fall damage
            // h = (v^2) / 2g
            // dmg = max(0, (h - 48) / 32) - half of blocks fallen starting from 3 blocks height
            //                int dmg = ((int)Math.max(0f, (((mob.getVelocity().y * mob.getVelocity().y) / (2 * gravity.y)) - 48f) / 16f));
            //                if (dmg > 0) System.out.println("Damage: " + dmg);

        } else {
            mob.y += 1;
            mob.setCanJump(checkColl(mob));
            mob.y -= 1;
        }

        if (mob.getY() > mGameWorld.getHeightPx()) {
            mob.kill();
        }
    }

    private void playerPhy(Player player, float delta) {
        if (player.isDead()) {
            return;
        }

        if (GameItems.isFluid(getBlock(player))) {
            if (mMainConfig.isTouch() && player.getVelocity().x != 0 && !player.swim && !player.isFlyMode()) {
                player.swim = true;
            }
            if (!player.swim) {
                if (!player.isFlyMode() && player.getVelocity().y < 32f) {
                    player.getVelocity().y += gravity.y * delta;
                }
                if (!player.isFlyMode() && player.getVelocity().y > 32f) {
                    player.getVelocity().y -= player.getVelocity().y * 32f * delta;
                }
            } else {
                player.getVelocity().y += PL_JUMP_VELOCITY * delta;
                if (player.getVelocity().y < -PL_SPEED) {
                    player.getVelocity().y = -PL_SPEED;
                }
            }
        } else {
            if (!player.isFlyMode() && player.getVelocity().y < PL_TERMINAL_VELOCITY) {
                player.getVelocity().y += gravity.y * delta;
            }
        }

        player.y += player.getVelocity().y * delta;
        mobYColl(player);

        player.x += player.getVelocity().x * (player.isFlyMode() ? 1.5f : 1) *
                (GameItems.isFluid(getBlock(player)) && !player.isFlyMode() ? .8f : 1) * delta;

        mobXColl(player);

        if (mMainConfig.isTouch() && !player.isFlyMode() && player.canJump() && player.getVelocity().x != 0 && checkJump(player)) {
            player.getVelocity().y = PL_JUMP_VELOCITY;
            player.setCanJump(false);
        }
    }

    private void mobPhy(Mob mob, float delta) {
        if (mob.getType() == Mob.Type.MOB && GameItems.isFluid(getBlock(mob))) {
            if (mob.getVelocity().y > 32f) {
                mob.getVelocity().y -= mob.getVelocity().y * 32f * delta;
            }

            mob.getVelocity().y += PL_JUMP_VELOCITY * delta;

            if (mob.getVelocity().y < -PL_SPEED) {
                mob.getVelocity().y = -PL_SPEED;
            }
        } else if (!mob.isFlyMode() && mob.getVelocity().y < PL_TERMINAL_VELOCITY) {
            mob.getVelocity().y += gravity.y * delta;
        }

        mob.y += mob.getVelocity().y * delta;
        mobYColl(mob);

        if (mob.isDead()) {
            return;
        }

        mob.x += mob.getVelocity().x * delta;
        mobXColl(mob);

        if (mob.canJump() && mob.getVelocity().x != 0 && checkJump(mob)) {
            mob.getVelocity().y = PL_JUMP_VELOCITY;
            mob.setCanJump(false);
        }
    }

    void update(float delta) {
        Player player = mMobsController.getPlayer();

        for (Iterator<Drop> it = mDropController.getIterator(); it.hasNext(); ) {
            Drop drop = it.next();
            dropPhy(drop, delta);
            if (Intersector.overlaps(drop, player)) {
                drop.pickUpDrop(player);
            }
            if (drop.isPickedUp()) {
                it.remove();
            }
        }

        for (Iterator<Mob> it = mMobsController.getIterator(); it.hasNext(); ) {
            Mob mob = it.next();
            mob.ai(mGameWorld, delta);
            mobPhy(mob, delta);
            if (mob.isDead()) {
                it.remove();
            }
        }

        playerPhy(player, delta);
        if (player.isDead()) {
            player.respawn(mGameWorld);
        }
    }

}
