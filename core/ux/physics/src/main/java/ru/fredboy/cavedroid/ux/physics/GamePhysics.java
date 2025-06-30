package ru.fredboy.cavedroid.ux.physics;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.fredboy.cavedroid.common.di.GameScope;
import ru.fredboy.cavedroid.common.utils.MeasureUnitsUtilsKt;
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository;
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository;
import ru.fredboy.cavedroid.domain.items.model.block.Block;
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem;
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase;
import ru.fredboy.cavedroid.entity.drop.model.Drop;
import ru.fredboy.cavedroid.entity.mob.model.FallingBlock;
import ru.fredboy.cavedroid.entity.mob.model.Mob;
import ru.fredboy.cavedroid.entity.mob.model.Player;
import ru.fredboy.cavedroid.game.controller.drop.DropController;
import ru.fredboy.cavedroid.game.controller.mob.MobController;
import ru.fredboy.cavedroid.game.world.GameWorld;

import javax.inject.Inject;
import java.util.Iterator;

@GameScope
public class GamePhysics {

    public static final float PL_JUMP_VELOCITY = -133.332f;
    public static final float PL_TERMINAL_VELOCITY = 1254.4f;

    private final Vector2 gravity = new Vector2(0, 444.44f);

    private final GameWorld mGameWorld;
    private final ApplicationContextRepository mGameContextRepository;
    private final MobController mMobController;
    private final DropController mDropController;
    private final GetItemByKeyUseCase mGetItemByKeyUseCase;

    @Inject
    public GamePhysics(GameWorld gameWorld,
                       ApplicationContextRepository gameContextRepository,
                       MobController mobController,
                       DropController dropController,
                       GetItemByKeyUseCase getItemByKeyUseCase) {
        mGameWorld = gameWorld;
        mGameContextRepository = gameContextRepository;
        mMobController = mobController;
        mDropController = dropController;
        mGetItemByKeyUseCase = getItemByKeyUseCase;
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

        if (checkColl(new Rectangle(blX, mob.getY() - 18, mob.getWidth(), mob.getHeight())) != null) {
            return false;
        }

        return (block.getParams().getHasCollision() && block.getParams().getCollisionMargins().getTop() < 8 &&
                (mob.getY() + mob.getHeight()) - block.getRectangle(blX / 16, blY / 16).y > 8);
    }

    /**
     * @return colliding rect or null if no collision
     */
    @Nullable
    private Rectangle checkColl(Rectangle rect) {
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
                if (block.getParams().getHasCollision()) {
                    final Rectangle blockRect = block.getRectangle(x, y);
                    if (Intersector.overlaps(rect, blockRect)) {
                        return blockRect;
                    }
                }
            }
        }

        return null;
    }

    private boolean isBlockToJump(Block block) {
        return block.getParams().getHasCollision() && block.getParams().getCollisionMargins().getTop() < 8;
    }

    private Block getBlock(Rectangle rect) {
        return mGameWorld.getForeMap((int) (rect.x + rect.width / 2) / 16,
                (int) (rect.y + rect.height / 8 * 7) / 16);
    }

    private Rectangle getShiftedPlayerRect(float shift) {
        final Player player = mMobController.getPlayer();
        return new Rectangle(player.x + shift, player.y, player.width, player.height);
    }

    /**
     * @return Rectangle representing magneting target for this drop
     */
    @Nullable
    private Rectangle getShiftedMagnetingPlayerRect(Drop drop) {
        final Player player = mMobController.getPlayer();

        if (!player.getInventory().canPickItem(drop.getItem())) {
            return null;
        }

        if (drop.canMagnetTo(player)) {
            return getShiftedPlayerRect(0);
        }

        final float fullWorldPx = MeasureUnitsUtilsKt.getPx(mGameWorld.getWidth());

        final Rectangle shiftedLeft = getShiftedPlayerRect(-fullWorldPx);
        if (drop.canMagnetTo(shiftedLeft)) {
            return shiftedLeft;
        }

        final Rectangle shiftedRight = getShiftedPlayerRect(fullWorldPx);
        if (drop.canMagnetTo(shiftedRight)) {
            return shiftedRight;
        }

        return null;
    }

    private void pickUpDropIfPossible(Rectangle shiftedPlayerTarget, Drop drop) {
        final Player player = mMobController.getPlayer();

        if (Intersector.overlaps(shiftedPlayerTarget, drop)) {
            drop.setPickedUp(player.getInventory().pickUpItem(drop.getInventoryItem()));
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

        if (checkColl(drop) != null) {
            dropVelocity.setZero();
            do {
                drop.y--;
            } while (checkColl(drop) != null);
        }

        if (playerMagnetTarget != null) {
            pickUpDropIfPossible(playerMagnetTarget, drop);
        }
    }

    private void mobXColl(Mob mob) {
        if (mob.getVelocity().x == 0f) {
            return;
        }

        @Nullable Rectangle collidingRect = checkColl(mob);

        if (collidingRect != null) {
            if (mob.getCanJump() && !mob.isFlyMode() && collidingRect.y >= mob.y + mob.height - 8) {
                mob.y = collidingRect.y - mob.height;
                return;
            }

            collidingRect = checkColl(mob);

            if (collidingRect != null) {
                int d = 0;

                if (mob.getVelocity().x < 0) {
                    d = 1;
                } else if (mob.getVelocity().x > 0) {
                    d = -1;
                }

                if (d < 0) {
                    mob.x = collidingRect.x - mob.width;
                } else {
                    mob.x = collidingRect.x + collidingRect.width;
                }

                if (mob.getCanJump()) {
                    mob.changeDir();
                }
            }
        }

        // Check world bounds
        final float worldWidthPx = MeasureUnitsUtilsKt.getPx(mGameWorld.getWidth());
        if (mob.getX() + mob.getWidth() / 2 < 0) {
            mob.x += worldWidthPx;
        }
        if (mob.getX() + mob.getWidth() / 2 > worldWidthPx) {
            mob.x -= worldWidthPx;
        }
    }

    private void mobYColl(Mob mob) {
        @Nullable final Rectangle collidingRect = checkColl(mob);
        if (collidingRect != null) {
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

            if (d < 0) {
                mob.y = collidingRect.y - mob.height;
            } else {
                mob.y = collidingRect.y + collidingRect.height;
            }

            mob.getVelocity().y = 0;

        } else {
            mob.y += 1;
            mob.setCanJump(checkColl(mob) != null);
            mob.y -= 1;
        }

        if (mob.getY() > MeasureUnitsUtilsKt.getPx(mGameWorld.getHeight())) {
            mob.kill();
        }
    }

    private void playerPhy(@NotNull Player player, float delta) {
        if (player.isDead()) {
            return;
        }

        if (getBlock(player).isFluid()) {
            if (mGameContextRepository.isTouch() && player.getVelocity().x != 0 && !player.getSwim() && !player.isFlyMode()) {
                player.setSwim(true);
            }
            if (!player.getSwim()) {
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

        if (mGameContextRepository.isTouch() && !player.isFlyMode() && player.getCanJump() && player.getVelocity().x != 0 && checkJump(player)) {
            player.jump();
            player.setCanJump(false);
        }
    }

    private void mobPhy(Mob mob, float delta) {
        if (!(mob instanceof FallingBlock) && getBlock(mob).isFluid()) {
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

        if (mob.getCanJump() && mob.getVelocity().x != 0 && checkJump(mob)) {
            mob.jump();
            mob.setCanJump(false);
        }
    }

    public void update(float delta) {
        Player player = mMobController.getPlayer();

        for (Iterator<Drop> it = mDropController.getAllDrop().iterator(); it.hasNext(); ) {
            Drop drop = it.next();
            dropPhy(drop, delta);
            if (drop.isPickedUp()) {
                it.remove();
            }
        }

        for (Iterator<Mob> it = mMobController.getMobs().iterator(); it.hasNext(); ) {
            Mob mob = it.next();
            //todo: Mob ai
//            mob.ai(mGameWorld, mGameItemsHolder, mMobController, delta);
            mobPhy(mob, delta);
            if (mob.isDead()) {
                for (InventoryItem invItem : mob.getDropItems(mGetItemByKeyUseCase)) {
                    mDropController.addDrop(mob.x, mob.y, invItem);
                }

                it.remove();
            }
        }

        playerPhy(player, delta);
        //todo : Update player
//        player.ai(mGameWorld, mGameItemsHolder, mMobController, delta);
        if (player.isDead()) {
//            for (InventoryItem invItem : player.getInventory().getItems()) {
//                mDropController.addDrop(player.x, player.y, invItem);
//            }
            player.getInventory().clear();
            mMobController.respawnPlayer();
        }
    }

}
