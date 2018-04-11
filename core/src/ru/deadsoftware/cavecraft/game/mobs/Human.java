package ru.deadsoftware.cavecraft.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.Assets;
import ru.deadsoftware.cavecraft.game.GameProc;

public class Human extends Mob{

    private RandomXS128 rand = new RandomXS128();
    private GameProc gameProc;

    public Human(int x, int y, GameProc gameProc) {
        this.gameProc = gameProc;
        position = new Vector2(x, y);
        moveX = new Vector2(0, 0);
        moveY = new Vector2(0, 0);
        width = 8;
        height = 30;
        dir = 1;
        canJump = false;
    }

    @Override
    public void ai() {
        if (canJump && gameProc.world.getForeMap(
                (int)(position.x/16)+(dir*2-1), (int)(position.y/16)+1)>0)
            moveY.add(0, -8);
        if (rand.nextInt(500)>490) dir++;
        if (dir>1) dir = 0;
        moveX.setZero();
        moveX.add(-2+4*dir, 0);
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y) {
        spriteBatch.draw(Assets.playerSkin[dir][0], x-2, y-2);
        if (Assets.playerSkin[0][2].getRotation()>=60 || Assets.playerSkin[0][2].getRotation()<=-60)
            Mob.ANIM_SPEED = -Mob.ANIM_SPEED;
        Assets.playerSkin[1][2].setPosition(x-6,y);
        Assets.playerSkin[1][2].draw(spriteBatch);
        Assets.playerSkin[1][3].setPosition(x-6, y+10);
        Assets.playerSkin[1][3].draw(spriteBatch);
        Assets.playerSkin[0][3].setPosition(x-6, y+10);
        Assets.playerSkin[0][3].draw(spriteBatch);
        spriteBatch.draw(Assets.playerSkin[dir][1], x-2, y + 8);

        Assets.playerSkin[0][2].setPosition(x-6, y);
        Assets.playerSkin[0][2].draw(spriteBatch);
    }

    public Rectangle getRect() {
        return new Rectangle(position.x, position.y, width, height);
    }
}
