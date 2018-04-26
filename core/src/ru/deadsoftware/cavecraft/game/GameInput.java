package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.TimeUtils;
import ru.deadsoftware.cavecraft.CaveGame;
import ru.deadsoftware.cavecraft.game.mobs.Pig;
import ru.deadsoftware.cavecraft.misc.AppState;
import ru.deadsoftware.cavecraft.misc.Assets;

public class GameInput {

    private GameProc gameProc;

    public GameInput(GameProc gameProc) {
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
                if (CaveGame.STATE == AppState.GAME_PLAY) CaveGame.STATE = AppState.GAME_CREATIVE_INV;
                    else CaveGame.STATE = AppState.GAME_PLAY;
                break;

            case Input.Keys.G:
                gameProc.mobs.add(new Pig(gameProc.cursorX*16, gameProc.cursorY*16));
                break;

            case Input.Keys.ESCAPE: case Input.Keys.BACK:
                CaveGame.STATE = AppState.GOTO_MENU;
                break;
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
        gameProc.touchDownTime = TimeUtils.millis();
        gameProc.isTouchDown = true;
        gameProc.touchDownButton = button;
        gameProc.touchDownX = screenX;
        gameProc.touchDownY = screenY;
    }

    public void touchUp(int screenX, int screenY, int button) {
        if (gameProc.isKeyDown) {
            keyUp(gameProc.keyDownCode);
            gameProc.isKeyDown = false;
        }
        if (gameProc.isTouchDown) {
            if (CaveGame.STATE == AppState.GAME_CREATIVE_INV &&
                    screenX>gameProc.renderer.camera.viewportWidth/2-Assets.creativeInv.getRegionWidth()/2 &&
                    screenX<gameProc.renderer.camera.viewportWidth/2+Assets.creativeInv.getRegionWidth()/2 &&
                    screenY>gameProc.renderer.camera.viewportHeight/2-Assets.creativeInv.getRegionHeight()/2 &&
                    screenY<gameProc.renderer.camera.viewportHeight/2+Assets.creativeInv.getRegionHeight()/2) {
                int ix = (int) (screenX - (gameProc.renderer.camera.viewportWidth / 2 - Assets.creativeInv.getRegionWidth() / 2 + 8)) / 18;
                int iy = (int) (screenY - (gameProc.renderer.camera.viewportHeight / 2 - Assets.creativeInv.getRegionHeight() / 2 + 18)) / 18;
                int item = gameProc.creativeScroll*8+(ix + iy * 8);
                if (ix>=8 || ix<0 || iy<0 || iy>=5) item=-1;
                if (item >= 0 && item < Items.BLOCKS.size) {
                    for (int i = 8; i > 0; i--) {
                        gameProc.player.inventory[i] = gameProc.player.inventory[i - 1];
                    }
                    gameProc.player.inventory[0] = item;
                }
            } else if (CaveGame.STATE == AppState.GAME_CREATIVE_INV) {
                CaveGame.STATE = AppState.GAME_PLAY;
            } else if (screenY<Assets.invBar.getRegionHeight() &&
                    screenX>gameProc.renderer.camera.viewportWidth/2-Assets.invBar.getRegionWidth()/2 &&
                    screenX<gameProc.renderer.camera.viewportWidth/2+Assets.invBar.getRegionWidth()/2) {
                gameProc.invSlot = (int)((screenX-(gameProc.renderer.camera.viewportWidth/2-Assets.invBar.getRegionWidth()/2))/20);
            } else if (button == Input.Buttons.RIGHT){
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
        if (CaveGame.STATE == AppState.GAME_CREATIVE_INV && Math.abs(screenY-gameProc.touchDownY)>16) {
            if (screenX>gameProc.renderer.camera.viewportWidth/2-Assets.creativeInv.getRegionWidth()/2 &&
                    screenX<gameProc.renderer.camera.viewportWidth/2+Assets.creativeInv.getRegionWidth()/2 &&
                    screenY>gameProc.renderer.camera.viewportHeight/2-Assets.creativeInv.getRegionHeight()/2 &&
                    screenY<gameProc.renderer.camera.viewportHeight/2+Assets.creativeInv.getRegionHeight()/2) {
                gameProc.creativeScroll -= (screenY - gameProc.touchDownY) / 16;
                gameProc.touchDownX = screenX;
                gameProc.touchDownY = screenY;
                if (gameProc.creativeScroll < 0) gameProc.creativeScroll = 0;
                if (gameProc.creativeScroll > gameProc.maxCreativeScroll)
                    gameProc.creativeScroll = gameProc.maxCreativeScroll;
            }
        }
    }

    public void scrolled(int amount) {
        switch (CaveGame.STATE) {
            case GAME_PLAY:
                gameProc.invSlot += amount;
                if (gameProc.invSlot < 0) gameProc.invSlot = 8;
                if (gameProc.invSlot > 8) gameProc.invSlot = 0;
                break;
            case GAME_CREATIVE_INV:
                gameProc.creativeScroll+=amount;
                if (gameProc.creativeScroll<0) gameProc.creativeScroll=0;
                if (gameProc.creativeScroll>gameProc.maxCreativeScroll)
                    gameProc.creativeScroll=gameProc.maxCreativeScroll;
                break;
        }
    }

}
