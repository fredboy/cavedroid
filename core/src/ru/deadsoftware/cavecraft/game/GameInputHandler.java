package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.TimeUtils;
import ru.deadsoftware.cavecraft.GameScreen;

public class GameInputHandler {

    private GameProc gameProc;

    public GameInputHandler(GameProc gameProc) {
        this.gameProc = gameProc;
    }

    public void  keyDown(int keyCode) {
        if (gameProc.ctrlMode==0) {
            if (keyCode == Input.Keys.A) {
                gameProc.player.moveX.add(-GamePhysics.PL_SPEED, 0);
                gameProc.player.dir = 0;
            }
            if (keyCode == Input.Keys.D) {
                gameProc.player.moveX.add(GamePhysics.PL_SPEED, 0);
                gameProc.player.dir = 1;
            }
        } else {
            if (keyCode == Input.Keys.A) {
                gameProc.cursorX--;
            }
            if (keyCode == Input.Keys.D) {
                gameProc.cursorX++;
            }
            if (keyCode == Input.Keys.W) {
                gameProc.cursorY--;
            }
            if (keyCode == Input.Keys.S) {
                gameProc.cursorY++;
            }
            if (gameProc.cursorX < 0)
                gameProc.cursorX = 0;
            if (gameProc.cursorX >= gameProc.world.getWidth())
                gameProc.cursorX = gameProc.world.getWidth()-1;
            if (gameProc.cursorY < 0)
                gameProc.cursorY = 0;
            if (gameProc.cursorY >= gameProc.world.getHeight())
                gameProc.cursorY = gameProc.world.getHeight()-1;
        }
        if (keyCode == Input.Keys.ALT_LEFT) {
            gameProc.ctrlMode++;
            gameProc.cursorX = (int)(gameProc.player.position.x/16);
            gameProc.cursorY = (int)(gameProc.player.position.y/16);
            if (gameProc.ctrlMode > 1) gameProc.ctrlMode = 0;
        }
        if (keyCode == Input.Keys.SPACE) {
             if (gameProc.player.canJump) {
                 gameProc.player.moveY.add(0, -7);
             } else if (!gameProc.player.flyMode) {
                 gameProc.player.flyMode = true;
                 gameProc.player.moveY.setZero();
             } else {
                 gameProc.player.moveY.y = -GamePhysics.PL_SPEED;
             }
        }
        if (keyCode == Input.Keys.CONTROL_LEFT) {
            gameProc.player.moveY.y = GamePhysics.PL_SPEED;
        }
    }

    public void keyUp(int keyCode) {
        if (keyCode == Input.Keys.A || keyCode == Input.Keys.D) {
            gameProc.player.moveX.x = 0;
        }
        if (keyCode == Input.Keys.SPACE) {
            if (gameProc.player.flyMode) gameProc.player.moveY.setZero();
        }
        if (keyCode == Input.Keys.CONTROL_LEFT) {
            if (gameProc.player.flyMode) gameProc.player.moveY.setZero();
        }
    }

    public void mouseMoved(int screenX, int screenY) {
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
    }

    public void scrolled(int amount) {
        gameProc.invSlot += amount;
        if (gameProc.invSlot < 0) gameProc.invSlot = 8;
        if (gameProc.invSlot > 8) gameProc.invSlot = 0;
    }

}
