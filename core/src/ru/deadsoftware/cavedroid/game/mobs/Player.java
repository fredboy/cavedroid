package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.game.GameItems;
import ru.deadsoftware.cavedroid.game.objects.Drop;
import ru.deadsoftware.cavedroid.game.objects.Item;
import ru.deadsoftware.cavedroid.game.world.GameWorld;
import ru.deadsoftware.cavedroid.misc.Assets;
import ru.deadsoftware.cavedroid.misc.utils.SpriteOrigin;
import ru.deadsoftware.cavedroid.misc.utils.SpriteUtilsKt;

import javax.annotation.CheckForNull;

public class Player extends Mob {

    public final int[] inventory;
    public int slot;
    public final int gameMode;
    public boolean swim;
    public float headRotation = 0f;

    public Player() {
        super(0, 0, 4, 30, randomDir(), Type.MOB);
        this.gameMode = 1;
        inventory = new int[9];
        swim = false;
    }

    public void respawn(GameWorld gameWorld) {
        Vector2 pos = getSpawnPoint(gameWorld);
        this.x = pos.x;
        this.y = pos.y;
        mVelocity.setZero();
        mDead = false;
    }

    public void pickUpDrop(Drop drop) {
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == 0 || inventory[i] == drop.getId()) {
                inventory[i] = drop.getId();
                drop.setPickedUp(true);
                break;
            }
        }
    }

    private Vector2 getSpawnPoint(GameWorld gameWorld) {
        int y;
        for (y = 0; y < gameWorld.getHeight(); y++) {
            if (y == gameWorld.getHeight() - 1) {
                y = 60;
                gameWorld.setForeMap(0, y, 1);
                break;
            }
            if (gameWorld.hasForeAt(0, y) && gameWorld.getForeMapBlock(0, y).hasCollision()) {
                break;
            }
        }
        return new Vector2(8 - getWidth() / 2, (float) y * 16 - getHeight());
    }

    public void setDir(Direction dir) {
        if (dir != getDirection()) {
            switchDir();
        }
    }

    @Override
    public void ai(GameWorld gameWorld, float delta) {
    }

    @Override
    public void changeDir() {
    }

    private void drawItem(SpriteBatch spriteBatch, float x, float y) {
        final int itemId = inventory[slot];
        final Item item = GameItems.getItem(itemId);

        @CheckForNull final Sprite sprite = item.isBlock()
                ? item.toBlock().getTexture()
                : item.getSprite();

        if (sprite == null) {
            return;
        }

        if (!item.isTool()) {
            sprite.setSize(Drop.DROP_SIZE, Drop.DROP_SIZE);
        }

        final float handLength = Assets.playerSprite[0][2].getHeight();

        final SpriteOrigin spriteOrigin = item.getDefaultOrigin();
        final int handMultiplier = 1 + -2 * dirMultiplier();
        final float xOffset = (-1 + dirMultiplier()) * sprite.getWidth() + 4 + handMultiplier * (sprite.getWidth() * spriteOrigin.getX());
        final float yOffset = item.isTool() ? -sprite.getHeight() / 2 : 0;

        float rotate = mAnim + 30;

        final float itemX = x + handLength * MathUtils.sin(handMultiplier * mAnim * MathUtils.degRad) + xOffset;
        final float itemY = y + handLength * MathUtils.cos(handMultiplier * mAnim * MathUtils.degRad) + yOffset;

        if (looksLeft()) {
            sprite.setFlip(true, sprite.isFlipY());
            SpriteUtilsKt.applyOrigin(sprite, spriteOrigin.getFlipped(true, false));
        } else {
            sprite.setFlip(false, sprite.isFlipY());
            SpriteUtilsKt.applyOrigin(sprite, spriteOrigin);
        }

        SpriteUtilsKt.draw(spriteBatch, sprite, itemX, itemY, -handMultiplier * rotate);

        // dont forget to reset
        sprite.setFlip(false, sprite.isFlipY());
        sprite.setRotation(0);
        sprite.setOriginCenter();
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y, float delta) {
        updateAnimation(delta);

        final Sprite backHand = Assets.playerSprite[1][2];
        final Sprite backLeg = Assets.playerSprite[1][3];
        final Sprite frontLeg = Assets.playerSprite[0][3];
        final Sprite head = Assets.playerSprite[dirMultiplier()][0];
        final Sprite body = Assets.playerSprite[dirMultiplier()][1];
        final Sprite frontHand = Assets.playerSprite[0][2];

        SpriteUtilsKt.draw(spriteBatch, backHand, x + 2, y + 8, -mAnim);

        if (looksLeft()) {
            drawItem(spriteBatch, x, y);
        }

        SpriteUtilsKt.draw(spriteBatch, backLeg, x + 2, y + 20, mAnim);
        SpriteUtilsKt.draw(spriteBatch, frontLeg, x + 2, y + 20, -mAnim);
        SpriteUtilsKt.draw(spriteBatch, head, x, y, headRotation);
        SpriteUtilsKt.draw(spriteBatch, body, x + 2, y + 8);

        if (looksRight()) {
            drawItem(spriteBatch, x, y);
        }

        SpriteUtilsKt.draw(spriteBatch, frontHand, x + 2, y + 8, mAnim);
    }

}
