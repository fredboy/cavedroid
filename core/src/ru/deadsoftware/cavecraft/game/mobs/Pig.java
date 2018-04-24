package ru.deadsoftware.cavecraft.game.mobs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.Assets;
import ru.deadsoftware.cavecraft.game.GameProc;

public class Pig extends Mob{

    private GameProc gameProc;

    public Pig(int x, int y, GameProc gameProc) {
        this.gameProc = gameProc;
        position = new Vector2(x, y);
        moveX = new Vector2(0, 0);
        moveY = new Vector2(0, 0);
        width = 25;
        height = 18;
        dir = 0;
        canJump = false;
    }

    @Override
    public void ai() {
        if (canJump && position.x>16 && position.x<(gameProc.world.getWidth()-1)*16 &&
                gameProc.world.getForeMap((int)(position.x/16)+(dir*2-1), (int)((position.y+height)/16))>0 &&
                gameProc.world.getForeMap((int)(position.x/16)+(dir*2-1), (int)((position.y)/16))==0)
            moveY.add(0, -8);
        if (MathUtils.randomBoolean(.0001f)) dir++;
        if (dir>1) dir = 0;
        moveX.set(-1.5f+3*dir,0);
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float x, float y) {
        //back legs
        Assets.pigSprite[1][1].setPosition(x-4+(9-dir*9),y+6);
        Assets.pigSprite[1][1].draw(spriteBatch);
        Assets.pigSprite[1][1].setPosition(x+17-(9*dir),y+6);
        Assets.pigSprite[1][1].draw(spriteBatch);
        //front legs
        Assets.pigSprite[0][1].setPosition(x-4+(9-dir*9),y+6);
        Assets.pigSprite[0][1].draw(spriteBatch);
        Assets.pigSprite[0][1].setPosition(x+17-(9*dir),y+6);
        Assets.pigSprite[0][1].draw(spriteBatch);
        //head & body
        spriteBatch.draw(Assets.pigSprite[dir][0], x, y);
    }

    @Override
    public Rectangle getRect() {
        return new Rectangle(position.x, position.y, width, height);
    }
}
