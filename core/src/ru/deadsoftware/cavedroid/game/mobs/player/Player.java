package ru.deadsoftware.cavedroid.game.mobs.player;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.game.GameItemsHolder;
import ru.deadsoftware.cavedroid.game.mobs.Mob;
import ru.deadsoftware.cavedroid.game.model.block.Block;
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem;
import ru.deadsoftware.cavedroid.game.model.item.Item;
import ru.deadsoftware.cavedroid.game.objects.Drop;
import ru.deadsoftware.cavedroid.game.ui.TooltipManager;
import ru.deadsoftware.cavedroid.game.world.GameWorld;
import ru.deadsoftware.cavedroid.misc.Assets;
import ru.deadsoftware.cavedroid.misc.utils.SpriteOrigin;
import ru.deadsoftware.cavedroid.misc.utils.SpriteUtilsKt;

import javax.annotation.CheckForNull;

public class Player extends Mob {

    private static final float SPEED = 69.072f;
    private static final float JUMP_VELOCITY = -133.332f;
    private static final int SURVIVAL_CURSOR_RANGE = 4;

    public static final int MAX_HEALTH = 20;
    public static final int INVENTORY_SIZE = 36;
    public static final int HOTBAR_SIZE = 9;

    private boolean hitting = false, hittingWithDamage = false;
    private float hitAnim = 0f;
    private float hitAnimDelta = ANIMATION_SPEED;

    public final Inventory inventory;

    public int gameMode;
    public boolean swim;
    public float headRotation = 0f;

    public float blockDamage = 0f;
    public int cursorX = 0;
    public int cursorY = 0;

    @CheckForNull
    private Vector2 spawnPoint = null;

    public ControlMode controlMode = ControlMode.WALK;

    public enum ControlMode {
        WALK,
        CURSOR
    }

    public Player(GameItemsHolder gameItemsHolder, TooltipManager tooltipManager) {
        super(0, 0, 4, 30, randomDir(), Type.MOB, MAX_HEALTH);
        inventory = new Inventory(INVENTORY_SIZE, HOTBAR_SIZE, gameItemsHolder, tooltipManager);
        swim = false;
    }

    public void initInventory(GameItemsHolder gameItemsHolder, TooltipManager tooltipManager) {
        inventory.initItems(gameItemsHolder, tooltipManager);
    }

    public void respawn(GameWorld gameWorld, GameItemsHolder itemsHolder) {
        Vector2 pos = getSpawnPoint(gameWorld, itemsHolder);
        this.x = pos.x;
        this.y = pos.y;
        mVelocity.setZero();
        mDead = false;
        heal(MAX_HEALTH);
    }

    public void decreaseCurrentItemCount(GameItemsHolder gameItemsHolder) {
        if (gameMode == 1) {
            return;
        }

        final InventoryItem item = inventory.getActiveItem();
        item.subtract();
        if (item.getAmount() <= 0) {
            setCurrentInventorySlotItem(gameItemsHolder.getFallbackItem());
        }
    }

    private Vector2 getSpawnPoint(GameWorld gameWorld, GameItemsHolder itemsHolder) {
        if (spawnPoint != null) {
            return spawnPoint;
        }

        int y, x = gameWorld.getWidth() / 2;
        for (y = 0; y <= gameWorld.getWorldConfig().getSeaLevel(); y++) {
            if (y == gameWorld.getWorldConfig().getSeaLevel()) {
                for (x = 0; x < gameWorld.getWidth(); x++) {
                    if (gameWorld.getForeMap(x, y).getParams().getHasCollision()) {
                        break;
                    }
                    if (x == gameWorld.getWidth() - 1) {
                        gameWorld.setForeMap(x, y, itemsHolder.getBlock("grass"));
                        break;
                    }
                }
                break;
            }
            if (gameWorld.hasForeAt(x, y) && gameWorld.getForeMap(x, y).hasCollision()) {
                break;
            }
        }
        spawnPoint = new Vector2(x * 16 + 8 - getWidth() / 2, (float) y * 16 - getHeight());
        return spawnPoint;
    }

    public void setDir(Direction dir) {
        if (dir != getDirection()) {
            switchDir();
        }
    }

    public void setCurrentInventorySlotItem(Item item) {
        inventory.getItems().set(inventory.getActiveSlot(), item.toInventoryItem());
    }

    @Override
    public float getSpeed() {
        return SPEED;
    }

    @Override
    public void jump() {
        if (!canJump()) {
            if (gameMode == 1) {
                if (isFlyMode()) {
                    setFlyMode(false);
                } else {
                    getVelocity().y = 0f;
                    setFlyMode(true);
                }
            }
            return;
        }
        mVelocity.y = JUMP_VELOCITY;
    }

    private boolean checkBlockCanBeHit(Block block) {
        return !block.isNone() && block.getParams().getHitPoints() >= 0;
    }

    private void hitBlock(GameWorld gameWorld, GameItemsHolder gameItemsHolder) {
        if (!hitting || !hittingWithDamage) {
            return;
        }

        final Block foregroundBlock = gameWorld.getForeMap(cursorX, cursorY);
        final Block backgroundBlock = gameWorld.getBackMap(cursorX, cursorY);


        if ((checkBlockCanBeHit(foregroundBlock)) ||
                (foregroundBlock.isNone() && checkBlockCanBeHit(backgroundBlock))) {
            if (gameMode == 0) {
                if (!foregroundBlock.isNone()) {
                    if (blockDamage >= foregroundBlock.getParams().getHitPoints()) {
                        gameWorld.destroyForeMap(cursorX, cursorY);
                        blockDamage = 0;
                    }
                } else if (!backgroundBlock.isNone()) {
                    if (blockDamage >= backgroundBlock.getParams().getHitPoints()) {
                        gameWorld.destroyBackMap(cursorX, cursorY);
                        blockDamage = 0;
                    }
                }
            } else {
                if (!foregroundBlock.isNone()) {
                    gameWorld.placeToForeground(cursorX, cursorY, gameItemsHolder.getFallbackBlock());
                } else if (!backgroundBlock.isNone()) {
                    gameWorld.placeToBackground(cursorX, cursorY, gameItemsHolder.getFallbackBlock());
                }
                stopHitting();
            }
        } else {
            stopHitting();
        }
    }

    @Override
    public void ai(GameWorld gameWorld, GameItemsHolder gameItemsHolder, float delta) {
        updateAnimation(delta);
        hitBlock(gameWorld, gameItemsHolder);

        if (gameMode == 1) {
            return;
        }

        final Block foregroundBlock = gameWorld.getForeMap(cursorX, cursorY);
        final Block backgroundBlock = gameWorld.getBackMap(cursorX, cursorY);
        @CheckForNull final Block target;

        if (checkBlockCanBeHit(foregroundBlock)) {
            target = foregroundBlock;
        } else if (checkBlockCanBeHit(backgroundBlock)) {
            target = backgroundBlock;
        } else {
            target = null;
        }

        final boolean canHitBlock = target != null;

        float multiplier = 1f;
        final Item currentItem = inventory.getActiveItem().getItem();
        if (currentItem instanceof Item.Tool && canHitBlock) {
            if (target.getParams().getToolType() == currentItem.getClass()
                    && ((Item.Tool)currentItem).getLevel() >= target.getParams().getToolLevel()) {
                multiplier = 2f * ((Item.Tool)currentItem).getLevel();
            }
            multiplier *= ((Item.Tool)currentItem).getBlockDamageMultiplier();
        }

        if (hitting && hittingWithDamage && canHitBlock) {
            blockDamage += 60f * delta * multiplier;
        } else {
            blockDamage = 0f;
        }
    }

    @Override
    public void changeDir() {
    }

    @Override
    public void damage(int damage) {
        if (gameMode == 1) {
            return;
        }

        if (damage > 0) {
            getVelocity().y += JUMP_VELOCITY / 3f;
        }

        super.damage(damage);
    }

    @Override
    public void heal(int heal) {
        if (gameMode == 1) {
            return;
        }
        super.heal(heal);
    }

    public void checkCursorBounds(GameWorld gameWorld) {
        if (gameMode == 0) {
            int minCursorX = getMapX() - SURVIVAL_CURSOR_RANGE;
            int maxCursorX = getMapX() + SURVIVAL_CURSOR_RANGE;
            int minCursorY = getMiddleMapY() - SURVIVAL_CURSOR_RANGE;
            int maxCursorY = getMiddleMapY() + SURVIVAL_CURSOR_RANGE;

            cursorX = MathUtils.clamp(cursorX, minCursorX, maxCursorX);
            cursorY = MathUtils.clamp(cursorY, minCursorY, maxCursorY);
        }

        cursorY = MathUtils.clamp(cursorY, 0, gameWorld.getHeight() - 1);
    }

    private void drawItem(SpriteBatch spriteBatch, float x, float y, float anim) {
        final Item item = inventory.getActiveItem().getItem();

        if (item == null || item.isNone()) {
            return;
        }

        final Sprite sprite = item.getSprite();
        final boolean smallSprite = !item.isTool() || item.isShears();

        final float originalWidth = sprite.getWidth();
        final float originalHeight = sprite.getHeight();

        if (smallSprite) {
            sprite.setSize(Drop.DROP_SIZE, Drop.DROP_SIZE);
        }

        final float handLength = Assets.playerSprite[0][2].getHeight();

        final SpriteOrigin spriteOrigin = item.getParams().getInHandSpriteOrigin();
        final int handMultiplier = -getDirection().getBasis();
        final float xOffset = (-1 + getDirection().getIndex()) * sprite.getWidth() + 4 + handMultiplier * (sprite.getWidth() * spriteOrigin.getX());
        final float yOffset = !smallSprite ? -sprite.getHeight() / 2 : 0;

        float rotate = anim + 30;

        if (item.isTool()) {
            sprite.rotate90(looksLeft());
        }

        final float itemX = x + handLength * MathUtils.sin(handMultiplier * anim * MathUtils.degRad) + xOffset;
        final float itemY = y + handLength * MathUtils.cos(handMultiplier * anim * MathUtils.degRad) + yOffset;

        if (looksLeft()) {
            sprite.setFlip(!item.isTool(), sprite.isFlipY());
            SpriteUtilsKt.applyOrigin(sprite, spriteOrigin.getFlipped(true, false));
        } else {
            sprite.setFlip(item.isTool(), sprite.isFlipY());
            SpriteUtilsKt.applyOrigin(sprite, spriteOrigin);
        }

        sprite.setRotation(-handMultiplier * rotate);
        sprite.setPosition(itemX, itemY);
        sprite.draw(spriteBatch);

        // dont forget to reset
        sprite.setFlip(false, sprite.isFlipY());
        sprite.setRotation(0);
        sprite.setOriginCenter();
        sprite.setSize(originalWidth, originalHeight);
        if (item.isTool()) {
            sprite.rotate90(looksRight());
        }
    }

    public void startHitting(boolean withDamage) {
        if (hitting) {
            return;
        }

        hitting = true;
        hittingWithDamage = withDamage;
        hitAnim = 90f;
        hitAnimDelta = ANIMATION_SPEED;
    }

    public void startHitting() {
        startHitting(true);
    }

    public void stopHitting() {
        blockDamage = 0f;
        hitting = false;
    }

    private float getRightHandAnim(float delta) {
        hitAnim -= hitAnimDelta * delta;

        if (hitAnim < 30f || hitAnim > 90f) {
            if (hitting) {
                hitAnim = MathUtils.clamp(hitAnim, 30f, 90f);
                hitAnimDelta = -hitAnimDelta;
            } else  {
                hitAnimDelta = ANIMATION_SPEED;
            }
        }

        if (!hitting) {
            if (hitAnim < hitAnimDelta * delta) {
                hitAnim = 0;
                hitAnimDelta = 0;
                return -mAnim;
            }
        }

        return hitAnim;
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y, float delta) {
        final Sprite backHand = Assets.playerSprite[1][2];
        final Sprite backLeg = Assets.playerSprite[1][3];
        final Sprite frontLeg = Assets.playerSprite[0][3];
        final Sprite head = Assets.playerSprite[getDirection().getIndex()][0];
        final Sprite body = Assets.playerSprite[getDirection().getIndex()][1];
        final Sprite frontHand = Assets.playerSprite[0][2];

        float backHandAnim, frontHandAnim;

        final float rightHandAnim = getRightHandAnim(delta);

        if (looksLeft()) {
            backHandAnim = rightHandAnim;
            frontHandAnim = mAnim;
        } else {
            backHandAnim = -mAnim;
            frontHandAnim = -rightHandAnim;
        }

        SpriteUtilsKt.drawSprite(spriteBatch, backHand, x + 2, y + 8, backHandAnim);

        if (looksLeft()) {
            drawItem(spriteBatch, x, y, -backHandAnim);
        }

        SpriteUtilsKt.drawSprite(spriteBatch, backLeg, x + 2, y + 20, mAnim);
        SpriteUtilsKt.drawSprite(spriteBatch, frontLeg, x + 2, y + 20, -mAnim);
        SpriteUtilsKt.drawSprite(spriteBatch, head, x, y, headRotation);
        SpriteUtilsKt.drawSprite(spriteBatch, body, x + 2, y + 8);

        if (looksRight()) {
            drawItem(spriteBatch, x, y, frontHandAnim);
        }

        SpriteUtilsKt.drawSprite(spriteBatch, frontHand, x + 2, y + 8, frontHandAnim);
    }

}
