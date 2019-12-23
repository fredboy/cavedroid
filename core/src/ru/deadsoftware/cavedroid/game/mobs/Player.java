package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavedroid.misc.Assets;

import static ru.deadsoftware.cavedroid.GameScreen.GP;

public class Player extends Mob {

    public final int[] inventory;
    public int slot;
    public final int gameMode;
    public boolean swim;

    public Player(int gameMode) {
        super(0, 0, 4, 30, randomDir(), Type.MOB);
        this.gameMode = gameMode;
        inventory = new int[9];
        swim = false;
    }

    public void respawn() {
        Vector2 pos = getSpawnPoint();
        this.x = pos.x;
        this.y = pos.y;
        move.setZero();
    }

    private Vector2 getSpawnPoint() {
        int x = 0, y;
        for (y = 0; y < GP.world.getHeight(); y++) {
            if (y == GP.world.getHeight() - 1) {
                y = 60;
                GP.world.setForeMap(x, y, 1);
                break;
            }
            if (GP.world.hasForeAt(x, y) && GP.world.getForeMapBlock(x, y).hasCollision()) {
                break;
            }
        }
        return new Vector2(x * 16 + 8 - getWidth() / 2, (float) y * 16 - getHeight());
    }

    public void setDir(Direction dir) {
        if (dir != getDirection()) {
            switchDir();
        }
    }

    @Override
    public void ai() {
    }

    @Override
    public void changeDir() {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y) {
        if (move.x != 0 || Assets.playerSprite[0][2].getRotation() != 0) {
            Assets.playerSprite[0][2].rotate(animDelta);
            Assets.playerSprite[1][2].rotate(-animDelta);
            Assets.playerSprite[0][3].rotate(-animDelta);
            Assets.playerSprite[1][3].rotate(animDelta);
        } else {
            Assets.playerSprite[0][2].setRotation(0);
            Assets.playerSprite[1][2].setRotation(0);
            Assets.playerSprite[0][3].setRotation(0);
            Assets.playerSprite[1][3].setRotation(0);
        }
        if (Assets.playerSprite[0][2].getRotation() >= 60 || Assets.playerSprite[0][2].getRotation() <= -60)
            animDelta = -animDelta;

        //back hand
        Assets.playerSprite[1][2].setPosition(x - 6, y);
        Assets.playerSprite[1][2].draw(spriteBatch);
        //back leg
        Assets.playerSprite[1][3].setPosition(x - 6, y + 10);
        Assets.playerSprite[1][3].draw(spriteBatch);
        //front leg
        Assets.playerSprite[0][3].setPosition(x - 6, y + 10);
        Assets.playerSprite[0][3].draw(spriteBatch);
        //head
        spriteBatch.draw(Assets.playerSprite[dirMultiplier()][0], x - 2, y - 2);
        //body
        spriteBatch.draw(Assets.playerSprite[dirMultiplier()][1], x - 2, y + 8);
        //front hand
        Assets.playerSprite[0][2].setPosition(x - 6, y);
        Assets.playerSprite[0][2].draw(spriteBatch);
    }

}
