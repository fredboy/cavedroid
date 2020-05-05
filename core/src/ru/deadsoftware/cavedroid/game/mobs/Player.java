package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.game.GameWorld;
import ru.deadsoftware.cavedroid.misc.Assets;

public class Player extends Mob {

    public final int[] inventory;
    public int slot;
    public final int gameMode;
    public boolean swim;

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
        mMove.setZero();
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
    public void ai(GameWorld gameWorld) {
    }

    @Override
    public void changeDir() {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y) {
        if (mMove.x != 0 || Assets.playerSprite[0][2].getRotation() != 0) {
            Assets.playerSprite[0][2].rotate(mAnimDelta);
            Assets.playerSprite[1][2].rotate(-mAnimDelta);
            Assets.playerSprite[0][3].rotate(-mAnimDelta);
            Assets.playerSprite[1][3].rotate(mAnimDelta);
        } else {
            Assets.playerSprite[0][2].setRotation(0);
            Assets.playerSprite[1][2].setRotation(0);
            Assets.playerSprite[0][3].setRotation(0);
            Assets.playerSprite[1][3].setRotation(0);
        }
        if (Assets.playerSprite[0][2].getRotation() >= 60 || Assets.playerSprite[0][2].getRotation() <= -60) {
            mAnimDelta = -mAnimDelta;
        }

        //back hand
        Assets.playerSprite[1][2].setPosition(x + 2, y + 8);
        Assets.playerSprite[1][2].draw(spriteBatch);
        //back leg
        Assets.playerSprite[1][3].setPosition(x + 2, y + 20);
        Assets.playerSprite[1][3].draw(spriteBatch);
        //front leg
        Assets.playerSprite[0][3].setPosition(x + 2, y + 20);
        Assets.playerSprite[0][3].draw(spriteBatch);
        //head
        spriteBatch.draw(Assets.playerSprite[dirMultiplier()][0], x, y);
        //body
        spriteBatch.draw(Assets.playerSprite[dirMultiplier()][1], x + 2, y + 8);
        //front hand
        Assets.playerSprite[0][2].setPosition(x + 2, y + 8);
        Assets.playerSprite[0][2].draw(spriteBatch);
    }

}
