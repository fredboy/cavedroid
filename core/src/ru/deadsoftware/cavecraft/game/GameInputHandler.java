package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.TimeUtils;

public class GameInputHandler {

    private GameProc gameProc;

    public GameInputHandler(GameProc gameProc) {
        this.gameProc = gameProc;
    }

    public void mouseMoved(int screenX, int screenY) {
        gameProc.cursorX = (int)((screenX+gameProc.renderer.camera.position.x)/32);
        gameProc.cursorY = (int)((screenY+gameProc.renderer.camera.position.y)/32);
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
        if (button == Input.Buttons.LEFT) {
            if (gameProc.world.getForeMap(gameProc.cursorX, gameProc.cursorY) > 0) {
                gameProc.world.placeToForeground(gameProc.cursorX, gameProc.cursorY, 0);
            } else if (gameProc.world.getBackMap(gameProc.cursorX, gameProc.cursorY) > 0) {
                gameProc.world.placeToBackground(gameProc.cursorX, gameProc.cursorY, 0);
            }
        } else {
            gameProc.touchDownTime = TimeUtils.millis();
            gameProc.isTouchDown = true;
        }
    }

    public void touchUp(int screenX, int screenY, int button) {
        if (gameProc.isTouchDown && button == Input.Buttons.RIGHT){
                gameProc.world.placeToForeground(gameProc.cursorX, gameProc.cursorY, 1);
        }
        gameProc.isTouchDown = false;
    }

    public void touchDragged(int screenX, int screenY) {
    }

}
