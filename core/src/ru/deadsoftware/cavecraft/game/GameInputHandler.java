package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.TimeUtils;
import ru.deadsoftware.cavecraft.*;

public class GameInputHandler {

    private GameProc gameProc;

    public GameInputHandler(GameProc gameProc) {
        this.gameProc = gameProc;
    }

    private void wasdPressed(int keycode) {
        if (gameProc.ctrlMode==0 || !CaveGame.TOUCH) {
            switch (keycode) {
                case Input.Keys.A:
                    gameProc.player.moveX.x = -GamePhysics.PL_SPEED;
                    gameProc.player.dir = 0;
                    break;
                case Input.Keys.D:
                    gameProc.player.moveX.x = GamePhysics.PL_SPEED;
                    gameProc.player.dir = 1;
                    break;
            }
        } else if (CaveGame.TOUCH){
            switch (keycode) {
                case Input.Keys.A:
                    gameProc.cursorX--;
                    break;
                case Input.Keys.D:
                    gameProc.cursorX++;
                    break;
                case Input.Keys.W:
                    gameProc.cursorY--;
                    break;
                case Input.Keys.S:
                    gameProc.cursorY++;
                    break;
            }
        }
    }

    public void  keyDown(int keycode) {
        gameProc.isKeyDown = true;
        gameProc.keyDownCode = keycode;
        if (keycode == Input.Keys.W || keycode == Input.Keys.A ||
                keycode == Input.Keys.S || keycode == Input.Keys.D) {
            wasdPressed(keycode);
        } else switch (keycode) {
            case Input.Keys.ALT_LEFT:
                if (CaveGame.TOUCH) {
                    gameProc.ctrlMode++;
                    if (gameProc.ctrlMode > 1) gameProc.ctrlMode = 0;
                }
                break;

            case Input.Keys.SPACE:
                if (gameProc.player.canJump) {
                    gameProc.player.moveY.add(0, -7);
                } else if (!gameProc.player.flyMode) {
                    gameProc.player.flyMode = true;
                    gameProc.player.moveY.setZero();
                } else {
                    gameProc.player.moveY.y = -GamePhysics.PL_SPEED;
                }
                break;

            case Input.Keys.CONTROL_LEFT:
                gameProc.player.moveY.y = GamePhysics.PL_SPEED;
                break;

            case Input.Keys.E:
                if (CaveGame.STATE == GameState.GAME_PLAY) CaveGame.STATE = GameState.GAME_CREATIVE_INV;
                    else CaveGame.STATE = GameState.GAME_PLAY;
                break;

            case Input.Keys.N:
                CaveGame.STATE = GameState.RESTART;
        }
    }

    public void keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.A: case Input.Keys.D:
                gameProc.player.moveX.x = 0;
                break;

            case Input.Keys.SPACE: case Input.Keys.CONTROL_LEFT:
                if (gameProc.player.flyMode) gameProc.player.moveY.setZero();
                break;
        }
    }

    public void mouseMoved(int screenX, int screenY) {
    }

    public void touchDown(int screenX, int screenY, int button) {
        if (CaveGame.STATE == GameState.GAME_CREATIVE_INV &&
                screenX>gameProc.renderer.camera.viewportWidth/2-Assets.creativeInv.getRegionWidth()/2 &&
                screenX<gameProc.renderer.camera.viewportWidth/2+Assets.creativeInv.getRegionWidth()/2 &&
                screenY>gameProc.renderer.camera.viewportHeight/2-Assets.creativeInv.getRegionHeight()/2 &&
                screenY<gameProc.renderer.camera.viewportHeight/2+Assets.creativeInv.getRegionHeight()/2) {
            int ix = (int) (screenX - (gameProc.renderer.camera.viewportWidth / 2 - Assets.creativeInv.getRegionWidth() / 2 + 8)) / 18;
            int iy = (int) (screenY - (gameProc.renderer.camera.viewportHeight / 2 - Assets.creativeInv.getRegionHeight() / 2 + 18)) / 18;
            int item = ix + iy * 8;
            if (item >= 0 && item < Items.BLOCKS.size) {
                for (int i = 8; i > 0; i--) {
                    gameProc.player.inventory[i] = gameProc.player.inventory[i - 1];
                }
                gameProc.player.inventory[0] = item;
            }
        } else if (CaveGame.STATE == GameState.GAME_CREATIVE_INV) {
            CaveGame.STATE = GameState.GAME_PLAY;
        } else {
            gameProc.touchDownX = screenX;
            gameProc.touchDownY = screenY;
            gameProc.touchDownTime = TimeUtils.millis();
            gameProc.isTouchDown = true;
            gameProc.touchDownButton = button;
        }
    }

    public void touchUp(int screenX, int screenY, int button) {
        if (gameProc.isKeyDown) {
            keyUp(gameProc.keyDownCode);
            gameProc.isKeyDown = false;
        }
        if (gameProc.isTouchDown) {
            if (button == Input.Buttons.RIGHT){
                gameProc.world.placeToForeground(gameProc.cursorX, gameProc.cursorY,
                            gameProc.player.inventory[gameProc.invSlot]);
            } else if (button == Input.Buttons.LEFT) {
                if (screenY<Assets.invBar.getRegionHeight() &&
                        screenX>gameProc.renderer.camera.viewportWidth/2-Assets.invBar.getRegionWidth()/2 &&
                        screenX<gameProc.renderer.camera.viewportWidth/2+Assets.invBar.getRegionWidth()/2) {
                    gameProc.invSlot = (int)((screenX-(gameProc.renderer.camera.viewportWidth/2-Assets.invBar.getRegionWidth()/2))/20);
                } else if (gameProc.world.getForeMap(gameProc.cursorX, gameProc.cursorY) > 0) {
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
