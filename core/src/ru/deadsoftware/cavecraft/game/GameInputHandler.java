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

    private void wasdPressed(int keyCode) {
        if (gameProc.ctrlMode==0) {
            switch (keyCode) {
                case Input.Keys.A:
                    gameProc.player.moveX.x = -GamePhysics.PL_SPEED;
                    gameProc.player.dir = 0;
                    break;
                case Input.Keys.D:
                    gameProc.player.moveX.x = GamePhysics.PL_SPEED;
                    gameProc.player.dir = 1;
                    break;
            }
        } else {
            switch (keyCode) {
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
            if (gameProc.cursorX<(gameProc.player.position.x+gameProc.player.texWidth/2)/16)
                gameProc.player.dir=0;
            if (gameProc.cursorX>(gameProc.player.position.x+gameProc.player.texWidth/2)/16)
                gameProc.player.dir=1;
        }
    }

    public void  keyDown(int keyCode) {
        if (keyCode == Input.Keys.W || keyCode == Input.Keys.A ||
                keyCode == Input.Keys.S || keyCode == Input.Keys.D) {
            wasdPressed(keyCode);
        } else switch (keyCode) {
            case Input.Keys.ALT_LEFT:
                gameProc.ctrlMode++;
                if (gameProc.ctrlMode > 1) gameProc.ctrlMode = 0;
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
        }
    }

    public void keyUp(int keyCode) {
        switch (keyCode) {
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
            try {
                int ix = (int) (screenX - (gameProc.renderer.camera.viewportWidth / 2 - Assets.creativeInv.getRegionWidth() / 2 + 8)) / 18;
                int iy = (int) (screenY - (gameProc.renderer.camera.viewportHeight / 2 - Assets.creativeInv.getRegionHeight() / 2 + 18)) / 18;
                int item = ix + iy * 8;
                for (int i = 8; i > 0; i--) {
                    gameProc.player.inventory[i] = gameProc.player.inventory[i - 1];
                }
                if (item >= 0 && item < Items.BLOCKS.size) gameProc.player.inventory[0] = item;
            } catch (Exception e) {
                Gdx.app.error("GameInputHandler", e.toString());
            }
        } else if (CaveGame.STATE == GameState.GAME_CREATIVE_INV) {
            CaveGame.STATE = GameState.GAME_PLAY;
        } else if (button == Input.Buttons.RIGHT &&
                !gameProc.player.canJump && !gameProc.player.flyMode) {
            gameProc.world.placeToForeground(gameProc.cursorX, gameProc.cursorY,
                    gameProc.player.inventory[gameProc.invSlot]);
        } else {
            gameProc.touchDownX = screenX;
            gameProc.touchDownY = screenY;
            gameProc.touchDownTime = TimeUtils.millis();
            gameProc.isTouchDown = true;
            gameProc.touchDownButton = button;
        }
    }

    public void touchUp(int screenX, int screenY, int button) {
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
