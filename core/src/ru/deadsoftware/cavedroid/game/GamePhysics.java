package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.mobs.Mob;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.mobs.Player;
import ru.deadsoftware.cavedroid.game.model.block.Block;
import ru.deadsoftware.cavedroid.game.objects.Drop;
import ru.deadsoftware.cavedroid.game.objects.DropController;
import ru.deadsoftware.cavedroid.game.world.GameWorld;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import java.util.Iterator;


@GameScope
public class GamePhysics {

    public static final float PL_JUMP_VELOCITY = -133.332f;
    public static final float PL_TERMINAL_VELOCITY = 1254.4f;

    private final Vector2 gravity = new Vector2(0, 444.44f);

    private final GameWorld mGameWorld;
    private final MainConfig mMainConfig;
    private final MobsController mMobsController;
    private final DropController mDropController;
    private final GameItemsHolder mGameItemsHolder;

    @Inject
    public GamePhysics(GameWorld gameWorld,
                       MainConfig mainConfig,
                       MobsController mobsController,
                       DropController dropController,
                       GameItemsHolder gameItemsHolder) {
        mGameWorld = gameWorld;
        mMainConfig = mainConfig;
        mMobsController = mobsController;
        mDropController = dropController;
        mGameItemsHolder = gameItemsHolder;
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
        Block block = mGameWorld.getForeMap(blX / 16, blY / 16);

        if (checkColl(new Rectangle(blX, mob.getY() - 18, mob.getWidth(), mob.getHeight()))) {
            return false;
        }

        return (block.toJump() &&
                (mob.getY() + mob.getHeight()) - block.getRectangle(blX / 16, blY / 16).y > 8);
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

        Block block;
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                if (!mGameWorld.hasForeAt(x, y)) {
                    continue;
                }
                block = mGameWorld.getForeMap(x, y);
                if (block.hasCollision()) {
                    if (Intersector.overlaps(rect, block.getRectangle(x, y))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private Block getBlock(Rectangle rect) {
        return mGameWorld.getForeMap((int) (rect.x + rect.width / 2) / 16,
                (int) (rect.y + rect.height / 8 * 7) / 16);
    }

    private Rectangle getShiftedPlayerRect(float shift) {
        final Player player = mMobsController.getPlayer();
        return new Rectangle(player.x + shift, player.y, player.width, player.height);
    }

    /**
     * @return Rectangle representing magneting target for this drop
     */
    @CheckForNull
    private Rectangle getShiftedMagnetingPlayerRect(Drop drop) {
        final Player player = mMobsController.getPlayer();

        if (player.canPickUpDrop(drop) < 0) {
            return null;
        }

        if (drop.canMagnetTo(player)) {
            return getShiftedPlayerRect(0);
        }

        final Rectangle shiftedLeft = getShiftedPlayerRect(-mGameWorld.getWidthPx());
        if (drop.canMagnetTo(shiftedLeft)) {
            return shiftedLeft;
        }

        final Rectangle shiftedRight = getShiftedPlayerRect(mGameWorld.getWidthPx());
        if (drop.canMagnetTo(shiftedRight)) {
            return shiftedRight;
        }

        return null;
    }

    private void pickUpDropIfPossible(Rectangle shiftedPlayerTarget, Drop drop) {
        final Player player = mMobsController.getPlayer();

        if (Intersector.overlaps(shiftedPlayerTarget, drop)) {
            player.pickUpDrop(drop);
        }
    }

    private void dropPhy(Drop drop, float delta) {
        final Rectangle playerMagnetTarget = getShiftedMagnetingPlayerRect(drop);
        final Vector2 dropVelocity = drop.getVelocity();


        if (playerMagnetTarget != null) {
            final Vector2 magnetVector = new Vector2(playerMagnetTarget.x - drop.x,
                    playerMagnetTarget.y - drop.y);
            magnetVector.nor().scl(Drop.MAGNET_VELOCITY * delta);
            dropVelocity.add(magnetVector);
        } else {
            dropVelocity.y += gravity.y * delta;
        }

        dropVelocity.x = MathUtils.clamp(dropVelocity.x, -Drop.MAGNET_VELOCITY, Drop.MAGNET_VELOCITY);
        dropVelocity.y = MathUtils.clamp(dropVelocity.y, -Drop.MAGNET_VELOCITY, Drop.MAGNET_VELOCITY);

        drop.x += dropVelocity.x * delta;
        drop.y += dropVelocity.y * delta;

        if (checkColl(drop)) {
            dropVelocity.setZero();
            do {
                drop.y--;
            } while (checkColl(drop));
        }

        if (playerMagnetTarget != null) {
            pickUpDropIfPossible(playerMagnetTarget, drop);
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

                int dmg = ((int)Math.max(0f, (((mob.getVelocity().y * mob.getVelocity().y) / (2 * gravity.y)) - 48f) / 16f));
                if (dmg > 0) {
                    mob.damage(dmg);
                }
            }

            mob.y = MathUtils.round(mob.getY());

            while (checkColl(mob)) {
                mob.y += d;
            }

            mob.getVelocity().y = 0;

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

        if (getBlock(player).isFluid()) {
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
                if (player.getVelocity().y < -player.getSpeed()) {
                    player.getVelocity().y = -player.getSpeed();
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
                (getBlock(player).isFluid() && !player.isFlyMode() ? .8f : 1) * delta;

        mobXColl(player);

        if (mMainConfig.isTouch() && !player.isFlyMode() && player.canJump() && player.getVelocity().x != 0 && checkJump(player)) {
            player.jump();
            player.setCanJump(false);
        }
    }

    private void mobPhy(Mob mob, float delta) {
        if (mob.getType() == Mob.Type.MOB && getBlock(mob).isFluid()) {
            if (mob.getVelocity().y > 32f) {
                mob.getVelocity().y -= mob.getVelocity().y * 32f * delta;
            }

            mob.getVelocity().y += PL_JUMP_VELOCITY * delta;

            if (mob.getVelocity().y < -mob.getSpeed()) {
                mob.getVelocity().y = -mob.getSpeed();
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
            mob.jump();
            mob.setCanJump(false);
        }
    }

    void update(float delta) {
        Player player = mMobsController.getPlayer();

        for (Iterator<Drop> it = mDropController.getIterator(); it.hasNext(); ) {
            Drop drop = it.next();
            dropPhy(drop, delta);
            if (drop.getPickedUp()) {
                it.remove();
            }
        }

        for (Iterator<Mob> it = mMobsController.getMobs().iterator(); it.hasNext(); ) {
            Mob mob = it.next();
            mob.ai(mGameWorld, mGameItemsHolder, delta);
            mobPhy(mob, delta);
            if (mob.isDead()) {
                it.remove();
            }
        }

        playerPhy(player, delta);
        player.ai(mGameWorld, mGameItemsHolder, delta);
        if (player.isDead()) {
            player.respawn(mGameWorld, mGameItemsHolder);
        }
    }

}
