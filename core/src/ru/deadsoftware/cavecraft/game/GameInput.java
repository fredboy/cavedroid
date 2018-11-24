package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.TimeUtils;
import ru.deadsoftware.cavecraft.CaveGame;
import ru.deadsoftware.cavecraft.GameScreen;
import ru.deadsoftware.cavecraft.game.mobs.Pig;
import ru.deadsoftware.cavecraft.misc.AppState;
import ru.deadsoftware.cavecraft.misc.Assets;

public class GameInput {

    private GameProc gp;

    public GameInput(GameProc gp) {
        this.gp = gp;
    }

    private boolean checkSwim() {
        return GameItems.isFluid(gp.world.getForeMap(gp.player.getMapX(), gp.player.getMapY()));
    }

    private boolean insideCreativeInv(int screenX, int screenY) {
        return (screenX > gp.renderer.getWidth() / 2 - Assets.creativeInv.getRegionWidth() / 2 &&
                screenX < gp.renderer.getWidth() / 2 + Assets.creativeInv.getRegionWidth() / 2 &&
                screenY > gp.renderer.getHeight() / 2 - Assets.creativeInv.getRegionHeight() / 2 &&
                screenY < gp.renderer.getHeight() / 2 + Assets.creativeInv.getRegionHeight() / 2);
    }

    private void wasdPressed(int keycode) {
        if (gp.ctrlMode == 0 || !CaveGame.TOUCH) {
            switch (keycode) {
                case Input.Keys.A:
                    gp.player.mov.x = -GamePhysics.PL_SPEED;
                    gp.player.setDir(0);
                    if (CaveGame.TOUCH && checkSwim()) gp.player.swim = true;
                    break;
                case Input.Keys.D:
                    gp.player.mov.x = GamePhysics.PL_SPEED;
                    gp.player.setDir(1);
                    if (CaveGame.TOUCH && checkSwim()) gp.player.swim = true;
                    break;
            }
        } else {
            switch (keycode) {
                case Input.Keys.A:
                    gp.curX--;
                    break;
                case Input.Keys.D:
                    gp.curX++;
                    break;
                case Input.Keys.W:
                    gp.curY--;
                    break;
                case Input.Keys.S:
                    gp.curY++;
                    break;
            }
            gp.blockDmg = 0;
        }
    }

    public void keyDown(int keycode) {
        gp.isKeyDown = true;
        gp.keyDownCode = keycode;
        if (keycode == Input.Keys.W || keycode == Input.Keys.A ||
                keycode == Input.Keys.S || keycode == Input.Keys.D) {
            wasdPressed(keycode);
        } else switch (keycode) {
            case Input.Keys.ALT_LEFT:
                if (CaveGame.TOUCH) {
                    gp.ctrlMode++;
                    if (gp.ctrlMode > 1) gp.ctrlMode = 0;
                }
                break;

            case Input.Keys.SPACE:
                if (checkSwim()) {
                    gp.player.swim = true;
                } else if (gp.player.canJump) {
                    gp.player.mov.add(0, -7);
                } else if (!gp.player.flyMode && gp.player.gameMode == 1) {
                    gp.player.flyMode = true;
                    gp.player.mov.y = 0;
                } else if (gp.player.flyMode) {
                    gp.player.mov.y = -GamePhysics.PL_SPEED;
                }
                break;

            case Input.Keys.CONTROL_LEFT:
                gp.player.mov.y = GamePhysics.PL_SPEED;
                break;

            case Input.Keys.E:
                if (CaveGame.STATE == AppState.GAME_PLAY) switch (gp.player.gameMode) {
                    case 0:
                        //TODO survival inv
                        break;
                    case 1:
                        CaveGame.STATE = AppState.GAME_CREATIVE_INV;
                        break;
                }
                else CaveGame.STATE = AppState.GAME_PLAY;
                break;

            case Input.Keys.G:
                gp.mobs.add(new Pig(gp.curX * 16, gp.curY * 16));
                break;

            case Input.Keys.ESCAPE:
            case Input.Keys.BACK:
                CaveGame.STATE = AppState.GOTO_MENU;
                break;

            case Input.Keys.F1:
                GameScreen.SHOW_DEBUG = !GameScreen.SHOW_DEBUG;
                break;
        }
    }

    public void keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.A:
            case Input.Keys.D:
                gp.player.mov.x = 0;
                if (CaveGame.TOUCH && gp.player.swim) gp.player.swim = false;
                break;

            case Input.Keys.SPACE:
            case Input.Keys.CONTROL_LEFT:
                if (gp.player.flyMode) gp.player.mov.y = 0;
                if (gp.player.swim) gp.player.swim = false;
                break;
        }
    }

    public void touchDown(int screenX, int screenY, int button) {
        gp.touchDownTime = TimeUtils.millis();
        gp.isTouchDown = true;
        gp.touchDownBtn = button;
        gp.touchDownX = screenX;
        gp.touchDownY = screenY;
    }

    public void touchUp(int screenX, int screenY, int button) {
        if (CaveGame.TOUCH && gp.isKeyDown) {
            keyUp(gp.keyDownCode);
            gp.isKeyDown = false;
        }
        if (gp.isTouchDown) {
            if (CaveGame.STATE == AppState.GAME_CREATIVE_INV && insideCreativeInv(screenX, screenY)) {
                int ix = (int) (screenX - (gp.renderer.getWidth() / 2 - Assets.creativeInv.getRegionWidth() / 2 + 8)) / 18;
                int iy = (int) (screenY - (gp.renderer.getHeight() / 2 - Assets.creativeInv.getRegionHeight() / 2 + 18)) / 18;
                int item = gp.creativeScroll * 8 + (ix + iy * 8);
                if (ix >= 8 || ix < 0 || iy < 0 || iy >= 5) item = -1;
                if (item >= 0 && item < GameItems.getItemsSize()) {
                    for (int i = 8; i > 0; i--) {
                        gp.player.inv[i] = gp.player.inv[i - 1];
                    }
                    gp.player.inv[0] = item;
                }
            } else if (CaveGame.STATE == AppState.GAME_CREATIVE_INV) {
                CaveGame.STATE = AppState.GAME_PLAY;
            } else if (screenY < Assets.invBar.getRegionHeight() &&
                    screenX > gp.renderer.getWidth() / 2 - Assets.invBar.getRegionWidth() / 2 &&
                    screenX < gp.renderer.getWidth() / 2 + Assets.invBar.getRegionWidth() / 2) {
                gp.player.invSlot = (int) ((screenX - (gp.renderer.getWidth() / 2 - Assets.invBar.getRegionWidth() / 2)) / 20);
            } else if (button == Input.Buttons.RIGHT) {
                gp.useItem(gp.curX, gp.curY,
                        gp.player.inv[gp.player.invSlot], false);
            } else if (button == Input.Buttons.LEFT) {
                gp.blockDmg = 0;
            }
        }
        gp.isTouchDown = false;
    }

    public void touchDragged(int screenX, int screenY) {
        if (CaveGame.STATE == AppState.GAME_CREATIVE_INV && Math.abs(screenY - gp.touchDownY) > 16) {
            if (insideCreativeInv(screenX, screenY)) {
                gp.creativeScroll -= (screenY - gp.touchDownY) / 16;
                gp.touchDownX = screenX;
                gp.touchDownY = screenY;
                if (gp.creativeScroll < 0) gp.creativeScroll = 0;
                if (gp.creativeScroll > gp.maxCreativeScroll)
                    gp.creativeScroll = gp.maxCreativeScroll;
            }
        }
    }

    public void scrolled(int amount) {
        switch (CaveGame.STATE) {
            case GAME_PLAY:
                gp.player.invSlot += amount;
                if (gp.player.invSlot < 0) gp.player.invSlot = 8;
                if (gp.player.invSlot > 8) gp.player.invSlot = 0;
                break;
            case GAME_CREATIVE_INV:
                gp.creativeScroll += amount;
                if (gp.creativeScroll < 0) gp.creativeScroll = 0;
                if (gp.creativeScroll > gp.maxCreativeScroll)
                    gp.creativeScroll = gp.maxCreativeScroll;
                break;
        }
    }

}
