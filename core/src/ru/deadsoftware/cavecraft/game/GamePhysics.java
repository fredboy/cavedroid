package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.game.objects.Player;

public class GamePhysics {

    public static final int PL_SPEED = 2;

    private GameProc gameProc;

    private Vector2 gravity;

    public GamePhysics(GameProc gameProc) {
        this.gameProc = gameProc;
        gravity = new Vector2(0,2);
    }

    private boolean checkColl(Rectangle rect) {
        int[] bl = new int [6];
        bl[0] = gameProc.world.getForeMap(((int)rect.x/16), ((int)rect.y/16));
        bl[1] = gameProc.world.getForeMap(((int)(rect.x+rect.width-1)/16), ((int)rect.y/16));
        bl[2] = gameProc.world.getForeMap(((int)rect.x/16), ((int)(rect.y+rect.height/2)/16));
        bl[3] = gameProc.world.getForeMap(((int)(rect.x+rect.width-1)/16), ((int)(rect.y+rect.height/2)/16));
        bl[4] = gameProc.world.getForeMap(((int)rect.x/16), ((int)(rect.y+rect.height-1)/16));
        bl[5] = gameProc.world.getForeMap(((int)(rect.x+rect.width-1)/16), ((int)(rect.y+(rect.height-1))/16));
        for (int b: bl) if (b>0) {
            return true;
        }
        return false;
    }

    private void movePlayer(Player pl) {
        pl.position.add(pl.moveX);
        if (checkColl(pl.getRect())) {
            int d = 0;
            if (pl.moveX.x<0) d=1; else if (pl.moveX.x>0) d=-1;
            while (checkColl(pl.getRect())) pl.position.x+=d;
            //pl.moveX.setZero();
        }
        pl.position.add(pl.moveY);
        if (checkColl(pl.getRect())) {
            int d = 0;
            if (pl.moveY.y<0) d=1; else if (pl.moveY.y>0) d=-1;
            while (checkColl(pl.getRect())) pl.position.y+=d;
            pl.moveY.setZero();
        }
        pl.moveY.add(gravity);
        switch (pl.dir) {
            case 0:
                gameProc.renderer.camTargetPos.x = pl.position.x-gameProc.renderer.camera.viewportWidth+100;
                break;
            case 1:
                gameProc.renderer.camTargetPos.x = pl.position.x-100;
                break;
        }
    }

    public void update(float delta) {
        movePlayer(gameProc.player);
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
