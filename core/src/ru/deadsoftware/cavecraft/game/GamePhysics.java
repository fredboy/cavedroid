package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.game.mobs.Mob;
import ru.deadsoftware.cavecraft.game.objects.Player;

public class GamePhysics {

    public static final int PL_SPEED = 2;

    private GameProc gameProc;

    private Vector2 gravity;

    public GamePhysics(GameProc gameProc) {
        this.gameProc = gameProc;
        gravity = new Vector2(0,1);
    }

    private boolean checkColl(Rectangle rect) {
        int[] bl = new int [6];
        bl[0] = gameProc.world.getForeMap(((int)(rect.x+2)/16), ((int)rect.y/16));
        bl[1] = gameProc.world.getForeMap(((int)(rect.x+rect.width-2)/16), ((int)rect.y/16));
        bl[2] = gameProc.world.getForeMap(((int)(rect.x+2)/16), ((int)(rect.y+rect.height/2)/16));
        bl[3] = gameProc.world.getForeMap(((int)(rect.x+rect.width-2)/16), ((int)(rect.y+rect.height/2)/16));
        bl[4] = gameProc.world.getForeMap(((int)(rect.x+2)/16), ((int)(rect.y+rect.height-1)/16));
        bl[5] = gameProc.world.getForeMap(((int)(rect.x+rect.width-2)/16), ((int)(rect.y+(rect.height-1))/16));
        for (int b: bl) if (b>0) {
            return true;
        }
        return false;
    }

    private void playerPhy(Player pl) {
        pl.position.add(pl.moveY);
        if (checkColl(pl.getRect())) {
            pl.canJump = true;
            int d = -1;
            if (pl.moveY.y<0) d=1; else if (pl.moveY.y>0) d=-1;
            pl.position.y = MathUtils.round(pl.position.y);
            while (checkColl(pl.getRect())) pl.position.y+=d;
            pl.moveY.setZero();
        } else {
            pl.canJump = false;
        }
        pl.moveY.add(gravity);
        pl.position.add(pl.moveX);
        if (pl.position.x<0 ||
                pl.position.x+pl.width>gameProc.world.getWidth()*16)
            pl.position.sub(pl.moveX);
        if (checkColl(pl.getRect())) {
            int d = 0;
            if (pl.moveX.x<0) d=1; else if (pl.moveX.x>0) d=-1;
            pl.position.x = MathUtils.round(pl.position.x);
            while (checkColl(pl.getRect())) pl.position.x+=d;
        }
        switch (pl.dir) {
            case 0:
                gameProc.renderer.camTargetPos.x = pl.position.x-gameProc.renderer.camera.viewportWidth+100;
                break;
            case 1:
                gameProc.renderer.camTargetPos.x = pl.position.x-100;
                break;
        }
        if (gameProc.renderer.camTargetPos.x < 0) gameProc.renderer.camTargetPos.x = 0;
        if (gameProc.renderer.camTargetPos.x + gameProc.renderer.camera.viewportWidth >
                gameProc.world.getWidth()*16)
            gameProc.renderer.camTargetPos.x = gameProc.world.getWidth()*16-gameProc.renderer.camera.viewportWidth;
    }

    private void mobPhy(Mob mob) {
        mob.position.add(mob.moveY);
        if (checkColl(mob.getRect())) {
            mob.canJump = true;
            int d = -1;
            if (mob.moveY.y<0) d=1; else if (mob.moveY.y>0) d=-1;
            mob.position.y = MathUtils.round(mob.position.y);
            while (checkColl(mob.getRect())) mob.position.y+=d;
            mob.moveY.setZero();
        } else {
            mob.canJump = false;
        }
        mob.moveY.add(gravity);
        mob.position.add(mob.moveX);
        if (mob.position.x<32 ||
                mob.position.x+mob.width>gameProc.world.getWidth()*16-32)
            mob.position.sub(mob.moveX);
        if (checkColl(mob.getRect())) {
            int d = 0;
            if (mob.moveX.x<0) d=1; else if (mob.moveX.x>0) d=-1;
            mob.position.x = MathUtils.round(mob.position.x);
            while (checkColl(mob.getRect())) mob.position.x+=d;
        }
}

    public void update(float delta) {

        for (Mob mob : gameProc.mobs) {
            mob.ai();
            mobPhy(mob);
        }
        playerPhy(gameProc.player);

        if (gameProc.renderer.camera.position.x - gameProc.renderer.camTargetPos.x <= 8 &&
                gameProc.renderer.camera.position.x - gameProc.renderer.camTargetPos.x >= -8) {
            gameProc.renderer.camera.position.x = gameProc.renderer.camTargetPos.x;
        }
        if (gameProc.renderer.camera.position.x > gameProc.renderer.camTargetPos.x) {
            gameProc.renderer.camera.position.sub(16,0,0);
        }
        if (gameProc.renderer.camera.position.x < gameProc.renderer.camTargetPos.x) {
            gameProc.renderer.camera.position.add(16,0,0);
        }
        gameProc.renderer.camera.position.y = gameProc.player.position.y+gameProc.player.height/2
                -gameProc.renderer.camera.viewportHeight/2;
    }

}
