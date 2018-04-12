package ru.deadsoftware.cavecraft.game.mobs;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.Assets;
import ru.deadsoftware.cavecraft.game.GameProc;

public class Human extends Mob{

    private RandomXS128 rand = new RandomXS128();
    private GameProc gameProc;
    private Sprite[][] tex;

    public Human(int x, int y, GameProc gameProc) {
        this.gameProc = gameProc;
        position = new Vector2(x, y);
        moveX = new Vector2(0, 0);
        moveY = new Vector2(0, 0);
        width = 8;
        height = 30;
        dir = 0;
        canJump = false;
        tex = Assets.playerSkin.clone();
        animation = 0;
        anim_d = 1;
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
        if (moveX.x!=0) {
            animation+=Mob.ANIM_SPEED*anim_d;
            if (animation<=-60 || animation>=60) anim_d=-anim_d;
        }
        tex[0][2].setRotation(animation);
        tex[1][2].setRotation(-animation);
        tex[0][3].setRotation(-animation);
        tex[1][3].setRotation(animation);
        spriteBatch.draw(tex[dir][0], x-2, y-2);
        if (tex[0][2].getRotation()>=60 || tex[0][2].getRotation()<=-60)
            Mob.ANIM_SPEED = -Mob.ANIM_SPEED;
        tex[1][2].setPosition(x-6,y);
        tex[1][2].draw(spriteBatch);
        tex[1][3].setPosition(x-6, y+10);
        tex[1][3].draw(spriteBatch);
        tex[0][3].setPosition(x-6, y+10);
        tex[0][3].draw(spriteBatch);
        spriteBatch.draw(tex[dir][1], x-2, y + 8);

        tex[0][2].setPosition(x-6, y);
        tex[0][2].draw(spriteBatch);
    }

    public Rectangle getRect() {
        return new Rectangle(position.x, position.y, width, height);
    }
}
