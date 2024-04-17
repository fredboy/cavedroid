package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.game.objects.Drop;
import ru.deadsoftware.cavedroid.game.world.GameWorld;
import ru.deadsoftware.cavedroid.misc.Assets;
import ru.deadsoftware.cavedroid.misc.utils.SpriteUtilsKt;

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
        SpriteUtilsKt.draw(spriteBatch, backLeg, x + 2, y + 20, mAnim);
        SpriteUtilsKt.draw(spriteBatch, frontLeg, x + 2, y + 20, -mAnim);
        SpriteUtilsKt.draw(spriteBatch, head, x, y, headRotation);
        SpriteUtilsKt.draw(spriteBatch, body, x + 2, y + 8);
        SpriteUtilsKt.draw(spriteBatch, frontHand, x + 2, y + 8, mAnim);
    }

}
