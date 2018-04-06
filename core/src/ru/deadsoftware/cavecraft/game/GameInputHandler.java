package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.TimeUtils;

public class GameInputHandler {

    private GameProc gameProc;

    public GameInputHandler(GameProc gameProc) {
        this.gameProc = gameProc;
    }

    public void  keyDown(int keyCode) {
        if (keyCode == Input.Keys.LEFT) {
            gameProc.player.moveX.add(-GamePhysics.PL_SPEED,0);
            gameProc.player.dir = 0;
        }
        if (keyCode == Input.Keys.RIGHT) {
            gameProc.player.moveX.add(GamePhysics.PL_SPEED,0);
            gameProc.player.dir = 1;
        }
        if (keyCode == Input.Keys.UP &&
                gameProc.player.canJump) gameProc.player.moveY.add(0,-8);
    }

    public void keyUp(int keyCode) {
        if (keyCode == Input.Keys.RIGHT || keyCode == Input.Keys.LEFT) {
            gameProc.player.moveX.x = 0;
        }
    }

    public void mouseMoved(int screenX, int screenY) {
        gameProc.cursorX = (int)((screenX+gameProc.renderer.camera.position.x)/16);
        gameProc.cursorY = (int)((screenY+gameProc.renderer.camera.position.y)/16);
        if (gameProc.cursorX < 0)
            gameProc.cursorX = 0;
        if (gameProc.cursorX >= gameProc.world.getWidth())
            gameProc.cursorX = gameProc.world.getWidth()-1;
        if (gameProc.cursorY < 0)
            gameProc.cursorY = 0;
        if (gameProc.cursorY >= gameProc.world.getHeight())
            gameProc.cursorY = gameProc.world.getHeight()-1;

    }

    public void touchDown(int screenX, int screenY, int button) {
        gameProc.touchDownX = screenX;
        gameProc.touchDownY = screenY;
        gameProc.touchDownTime = TimeUtils.millis();
        gameProc.isTouchDown = true;
    }

    public void touchUp(int screenX, int screenY, int button) {
        if (gameProc.isTouchDown) {
            if (button == Input.Buttons.RIGHT){
                gameProc.world.placeToForeground(gameProc.cursorX, gameProc.cursorY,
                        gameProc.player.inventory[gameProc.invSlot]);
            } else if (button == Input.Buttons.LEFT) {
                if (gameProc.world.getForeMap(gameProc.cursorX, gameProc.cursorY) > 0) {
                    gameProc.world.placeToForeground(gameProc.cursorX, gameProc.cursorY, 0);
                } else if (gameProc.world.getBackMap(gameProc.cursorX, gameProc.cursorY) > 0) {
                    gameProc.world.placeToBackground(gameProc.cursorX, gameProc.cursorY, 0);
                }
            }
        }
        gameProc.isTouchDown = false;
    }

    public void touchDragged(int screenX, int screenY) {
        /*gameProc.renderer.camera.position.x += (gameProc.touchDownX-screenX);
        gameProc.renderer.camera.position.y += (gameProc.touchDownY-screenY);
        gameProc.touchDownX = screenX;
        gameProc.touchDownY = screenY;
        gameProc.isTouchDown = false;*/
    }

    public void scrolled(int amount) {
        gameProc.invSlot += amount;
        if (gameProc.invSlot < 0) gameProc.invSlot = 8;
        if (gameProc.invSlot > 8) gameProc.invSlot = 0;
    }

}
