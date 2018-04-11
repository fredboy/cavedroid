package ru.deadsoftware.cavecraft.game.mobs;

import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
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

    public Rectangle getRect() {
        return new Rectangle(position.x, position.y, width, height);
    }
}
