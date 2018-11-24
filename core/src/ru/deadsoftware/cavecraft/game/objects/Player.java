package ru.deadsoftware.cavecraft.game.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.game.GameItems;
import ru.deadsoftware.cavecraft.game.GameWorld;
import ru.deadsoftware.cavecraft.game.mobs.Mob;
import ru.deadsoftware.cavecraft.misc.Assets;

import java.io.Serializable;

public class Player extends Mob implements Serializable {

    public int[] inv;
    public int invSlot;
    public int gameMode;
    public boolean swim;

    public Player(GameWorld world, int gameMode) {
        super(0, 0, 4, 30, 1, true);
        this.gameMode = gameMode;
        inv = new int[9];
        pos = getSpawnPoint(world).cpy();
        swim = false;
    }

    public void respawn(GameWorld world) {
        pos.set(getSpawnPoint(world));
        mov.setZero();
    }

    private Vector2 getSpawnPoint(GameWorld world) {
        int x = 0, y;
        for (y = 0; y < world.getHeight(); y++) {
            if (y == world.getHeight() - 1) {
                y = 60;
                world.setForeMap(x, y, 1);
                break;
            }
            if (world.getForeMap(x, y) > 0 && GameItems.getBlock(world.getForeMap(x, y)).hasCollision()) break;
        }
        return new Vector2(x * 16 + 8 - (float) getWidth() / 2, (float) y * 16 - getHeight());
    }

    public void setDir(int dir) {
        if (dir != getDir()) changeDir();
    }

    @Override
    public void ai() {
    }

    @Override
    public void changeDir() {
        switchDir();
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y) {
        if (mov.x != 0 || Assets.plSprite[0][2].getRotation() != 0) {
            Assets.plSprite[0][2].rotate(animDelta);
            Assets.plSprite[1][2].rotate(-animDelta);
            Assets.plSprite[0][3].rotate(-animDelta);
            Assets.plSprite[1][3].rotate(animDelta);
        } else {
            Assets.plSprite[0][2].setRotation(0);
            Assets.plSprite[1][2].setRotation(0);
            Assets.plSprite[0][3].setRotation(0);
            Assets.plSprite[1][3].setRotation(0);
        }
        if (Assets.plSprite[0][2].getRotation() >= 60 || Assets.plSprite[0][2].getRotation() <= -60)
            animDelta = -animDelta;

        //back hand
        Assets.plSprite[1][2].setPosition(x - 6, y);
        Assets.plSprite[1][2].draw(spriteBatch);
        //back leg
        Assets.plSprite[1][3].setPosition(x - 6, y + 10);
        Assets.plSprite[1][3].draw(spriteBatch);
        //front leg
        Assets.plSprite[0][3].setPosition(x - 6, y + 10);
        Assets.plSprite[0][3].draw(spriteBatch);
        //head
        spriteBatch.draw(Assets.plSprite[getDir()][0], x - 2, y - 2);
        //body
        spriteBatch.draw(Assets.plSprite[getDir()][1], x - 2, y + 8);
        //item in hand
        if (inv[invSlot] > 0) {
            float handRotation = MathUtils.degRad * Assets.plSprite[0][2].getRotation();
            switch (GameItems.getItem(inv[invSlot]).getType()) {
                case 0:
                    Assets.blockTex[GameItems.getItem(inv[invSlot]).getTex()].setPosition(
                            x - 8 * MathUtils.sin(handRotation),
                            y + 6 + 8 * MathUtils.cos(handRotation));
                    Assets.blockTex[GameItems.getItem(inv[invSlot]).getTex()].draw(spriteBatch);
                    break;
                default:
                    Assets.itemTex[GameItems.getItem(inv[invSlot]).getTex()].flip((getDir() == 0), false);
                    Assets.itemTex[GameItems.getItem(inv[invSlot]).getTex()].setRotation(
                            -45 + getDir() * 90 + Assets.plSprite[0][2].getRotation());
                    Assets.itemTex[GameItems.getItem(inv[invSlot]).getTex()].setPosition(
                            x - 10 + (12 * getDir()) - 8 * MathUtils.sin(handRotation),
                            y + 2 + 8 * MathUtils.cos(handRotation));
                    Assets.itemTex[GameItems.getItem(inv[invSlot]).getTex()].draw(spriteBatch);
                    Assets.itemTex[GameItems.getItem(inv[invSlot]).getTex()].flip((getDir() == 0), false);
                    break;
            }
        }
        //front hand
        Assets.plSprite[0][2].setPosition(x - 6, y);
        Assets.plSprite[0][2].draw(spriteBatch);
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public Rectangle getRect() {
        return new Rectangle(pos.x, pos.y, getWidth(), getHeight());
    }

}
