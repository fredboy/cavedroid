package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.TimeUtils;
import com.google.common.collect.Range;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.GameScreen;
import ru.deadsoftware.cavedroid.game.mobs.Mob;
import ru.deadsoftware.cavedroid.game.mobs.Pig;
import ru.deadsoftware.cavedroid.misc.Assets;
import ru.deadsoftware.cavedroid.misc.ControlMode;
import ru.deadsoftware.cavedroid.misc.states.AppState;
import ru.deadsoftware.cavedroid.misc.states.GameState;

import static ru.deadsoftware.cavedroid.GameScreen.GP;
import static ru.deadsoftware.cavedroid.game.GameItems.*;

public class GameInput {

    private boolean keyDown, touchedDown;

    private int keyDownCode, touchDownBtn;
    private float touchDownX, touchDownY;
    private long touchDownTime;

    private int curX, curY;
    private int creativeScroll;
    private int blockDamage;

    private boolean checkSwim() {
        return GameItems.isFluid(GP.world.getForeMap(GP.player.getMapX(), GP.player.getLowerMapY()));
    }

    private void goUpwards() {
        if (checkSwim()) {
            GP.player.swim = true;
        } else if (GP.player.canJump()) {
            GP.player.getMov().add(0, -7);
        } else if (!GP.player.isFlyMode() && GP.player.gameMode == 1) {
            GP.player.setFlyMode(true);
            GP.player.getMov().y = 0;
        } else if (GP.player.isFlyMode()) {
            GP.player.getMov().y = -GamePhysics.PL_SPEED;
        }
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private boolean insideCreativeInv(float screenX, float screenY) {
        TextureRegion creative = Assets.textureRegions.get("creative");
        return (screenX > GP.renderer.getWidth() / 2 - creative.getRegionWidth() / 2 &&
                screenX < GP.renderer.getWidth() / 2 + creative.getRegionWidth() / 2 &&
                screenY > GP.renderer.getHeight() / 2 - creative.getRegionHeight() / 2 &&
                screenY < GP.renderer.getHeight() / 2 + creative.getRegionHeight() / 2);
    }

    private void wasdPressed(int keycode) {
        if (GP.controlMode == ControlMode.WALK || !CaveGame.TOUCH) {
            switch (keycode) {
                case Input.Keys.A:
                    GP.player.getMov().x = -GamePhysics.PL_SPEED;
                    GP.player.setDir(Mob.LEFT);
                    if (CaveGame.TOUCH && checkSwim()) GP.player.swim = true;
                    break;
                case Input.Keys.D:
                    GP.player.getMov().x = GamePhysics.PL_SPEED;
                    GP.player.setDir(Mob.RIGHT);
                    if (CaveGame.TOUCH && checkSwim()) GP.player.swim = true;
                    break;
                case Input.Keys.W:
                case Input.Keys.SPACE:
                    goUpwards();
                    break;
                case Input.Keys.S:
                case Input.Keys.CONTROL_LEFT:
                    GP.player.getMov().y = GamePhysics.PL_SPEED;
                    break;
            }
        } else {
            switch (keycode) {
                case Input.Keys.A:
                    curX--;
                    break;
                case Input.Keys.D:
                    curX++;
                    break;
                case Input.Keys.W:
                    curY--;
                    break;
                case Input.Keys.S:
                    curY++;
                    break;
            }
            blockDamage = 0;
        }
    }

    private boolean isNotAutoselectable(int x, int y) {
        return (!GP.world.hasForeAt(x, y) || !GP.world.getForeMapBlock(x, y).hasCollision());
    }

    private void checkCursorBounds() {
        if (curY < 0) {
            curY = 0;
        } else if (curY >= GP.world.getHeight()) {
            curY = GP.world.getHeight() - 1;
        }

        if (GP.controlMode == ControlMode.CURSOR) {
            if (curX * 16 + 8 < GP.player.getX() + GP.player.getWidth() / 2) {
                GP.player.setDir(Mob.LEFT);
            } else {
                GP.player.setDir(Mob.RIGHT);
            }
        }
    }

    private void moveCursor() {
        int pastX = curX;
        int pastY = curY;

        if (GP.controlMode == ControlMode.WALK && CaveGame.TOUCH) {
            curX = GP.player.getMapX() + (GP.player.looksLeft() ? -1 : 1);
            curY = GP.player.getUpperMapY();
            for (int i = 0; i < 2 && isNotAutoselectable(curX, curY); i++) {
                curY++;
            }
            if (isNotAutoselectable(curX, curY)) {
                curX += GP.player.looksLeft() ? 1 : -1;
            }
        } else if (!CaveGame.TOUCH) {
            curX = (int) (Gdx.input.getX() * (GP.renderer.getWidth() / GameScreen.getWidth()) + GP.renderer.getCamX()) / 16;
            curY = (int) (Gdx.input.getY() * (GP.renderer.getHeight() / GameScreen.getHeight()) + GP.renderer.getCamY()) / 16;
            if (curX < 0) curX--;
        }

        if (pastX != curX || pastY != curY) {
            blockDamage = 0;
        }

        checkCursorBounds();
    }

    private void useItem(int x, int y, int id, boolean bg) {
        String key = getItem(id).isBlock() ? getBlockKey(id) : getItemKey(id);
        if (id > 0) {
            if (getItem(id).isBlock()) {
                if (!bg) {
                    GP.world.placeToForeground(x, y, getBlockIdByItemId(id));
                } else {
                    GP.world.placeToBackground(x, y, getBlockIdByItemId(id));
                }
            } else {
                switch (key) {
                    case "bucket_water":
                        GP.world.placeToForeground(x, y, getBlockId("water"));
                        GP.player.inventory[GP.player.slot] = getItemId("bucket_empty");
                        break;
                    case "bucket_lava":
                        GP.world.placeToForeground(x, y, getBlockId("lava"));
                        GP.player.inventory[GP.player.slot] = getItemId("bucket_empty");
                        break;
                }
            }
        }
    }

    private void pressLMB() {
        if ((GP.world.hasForeAt(curX, curY) && GP.world.getForeMapBlock(curX, curY).getHp() >= 0) ||
                (!GP.world.hasForeAt(curX, curY) && GP.world.hasBackAt(curX, curY) &&
                        GP.world.getBackMapBlock(curX, curY).getHp() >= 0)) {
            if (GP.player.gameMode == 0) {
                blockDamage++;
                if (GP.world.hasForeAt(curX, curY)) {
                    if (blockDamage >= GP.world.getForeMapBlock(curX, curY).getHp()) {
                        GP.world.destroyForeMap(curX, curY);
                        blockDamage = 0;
                    }
                } else if (GP.world.hasBackAt(curX, curY)) {
                    if (blockDamage >= GP.world.getBackMapBlock(curX, curY).getHp()) {
                        GP.world.destroyBackMap(curX, curY);
                        blockDamage = 0;
                    }
                }
            } else {
                if (GP.world.hasForeAt(curX, curY)) {
                    GP.world.placeToForeground(curX, curY, 0);
                } else if (GP.world.hasBackAt(curX, curY)) {
                    GP.world.placeToBackground(curX, curY, 0);
                }
                touchedDown = false;
            }
        }
    }

    private boolean insideHotbar(float x, float y) {
        TextureRegion hotbar = Assets.textureRegions.get("hotbar");
        return y < hotbar.getRegionHeight() &&
                Range.open(GP.renderer.getWidth() / 2 - (float) hotbar.getRegionWidth() / 2,
                        GP.renderer.getWidth() / 2 + (float) hotbar.getRegionWidth() / 2).contains(x);
    }

    private void holdMB() {
        if (touchDownBtn == Input.Buttons.RIGHT) {
            useItem(curX, curY, GP.player.inventory[GP.player.slot], true);
            touchedDown = false;
        } else {
            if (insideHotbar(touchDownX, touchDownY)) {
                CaveGame.GAME_STATE = GameState.CREATIVE_INV;
                touchedDown = false;
            }
        }
    }

    public void keyDown(int keycode) {
        keyDown = true;
        keyDownCode = keycode;
        switch (keycode) {
            case Input.Keys.A:
            case Input.Keys.D:
            case Input.Keys.W:
            case Input.Keys.S:
            case Input.Keys.SPACE:
            case Input.Keys.CONTROL_LEFT:
                wasdPressed(keycode);
                break;

            case Input.Keys.ALT_LEFT:
                if (CaveGame.TOUCH) {
                    GP.controlMode = GP.controlMode == ControlMode.WALK ? ControlMode.CURSOR : ControlMode.WALK;
                }
                break;

            case Input.Keys.E:
                if (CaveGame.GAME_STATE == GameState.PLAY) {
                    switch (GP.player.gameMode) {
                        case 0:
                            //TODO survival inv
                            break;
                        case 1:
                            CaveGame.GAME_STATE = GameState.CREATIVE_INV;
                            break;
                    }
                } else {
                    CaveGame.GAME_STATE = GameState.PLAY;
                }
                break;

            case Input.Keys.G:
                GP.mobs.add(new Pig(curX * 16, curY * 16));
                break;

            case Input.Keys.Q:
                GP.world.placeToForeground(curX, curY, 8);
                break;

            case Input.Keys.ESCAPE:
            case Input.Keys.BACK:
                CaveGame.APP_STATE = AppState.SAVE;
                CaveGame.GAME_STATE = GameState.PAUSE;
                break;

            case Input.Keys.F1:
                GameScreen.SHOW_DEBUG = !GameScreen.SHOW_DEBUG;
                break;

            case Input.Keys.M:
                GameScreen.SHOW_MAP = !GameScreen.SHOW_MAP;
                break;
        }
    }

    public void keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.A:
            case Input.Keys.D:
                GP.player.getMov().x = 0;
                if (CaveGame.TOUCH && GP.player.swim) GP.player.swim = false;
                break;

            case Input.Keys.W:
            case Input.Keys.S:
            case Input.Keys.SPACE:
            case Input.Keys.CONTROL_LEFT:
                if (GP.player.isFlyMode()) GP.player.getMov().y = 0;
                if (GP.player.swim) GP.player.swim = false;
                break;
        }
    }

    public void touchDown(float touchX, float touchY, int button) {
        touchDownTime = TimeUtils.millis();
        touchedDown = true;
        touchDownBtn = button;
        touchDownX = touchX;
        touchDownY = touchY;
    }

    public void touchUp(float screenX, float screenY, int button) {
        if (CaveGame.TOUCH && keyDown) {
            keyUp(keyDownCode);
            keyDown = false;
        }
        TextureRegion hotbar = Assets.textureRegions.get("hotbar");
        TextureRegion creative = Assets.textureRegions.get("creative");
        if (touchedDown) {
            if (CaveGame.GAME_STATE == GameState.CREATIVE_INV && insideCreativeInv(screenX, screenY)) {
                int ix = (int) (screenX - (GP.renderer.getWidth() / 2 - creative.getRegionWidth() / 2 + 8)) / 18;
                int iy = (int) (screenY - (GP.renderer.getHeight() / 2 - creative.getRegionHeight() / 2 + 18)) / 18;
                int item = creativeScroll * 8 + (ix + iy * 8);
                if (ix >= 8 || ix < 0 || iy < 0 || iy >= 5) item = -1;
                if (item >= 0 && item < GameItems.getItemsSize()) {
                    System.arraycopy(GP.player.inventory, 0, GP.player.inventory, 1, 8);
                    GP.player.inventory[0] = item;
                }
            } else if (CaveGame.GAME_STATE == GameState.CREATIVE_INV) {
                CaveGame.GAME_STATE = GameState.PLAY;
            } else if (screenY < hotbar.getRegionHeight() &&
                    screenX > GP.renderer.getWidth() / 2 - (float) hotbar.getRegionWidth() / 2 &&
                    screenX < GP.renderer.getWidth() / 2 + (float) hotbar.getRegionWidth() / 2) {
                GP.player.slot = (int) ((screenX - (GP.renderer.getWidth() / 2 - hotbar.getRegionWidth() / 2)) / 20);
            } else if (button == Input.Buttons.RIGHT) {
                useItem(curX, curY,
                        GP.player.inventory[GP.player.slot], false);
            } else if (button == Input.Buttons.LEFT) {
                blockDamage = 0;
            }
        }
        touchedDown = false;
    }

    public void touchDragged(float screenX, float screenY) {
        if (CaveGame.GAME_STATE == GameState.CREATIVE_INV && Math.abs(screenY - touchDownY) > 16) {
            if (insideCreativeInv(screenX, screenY)) {
                creativeScroll -= (screenY - touchDownY) / 16;
                touchDownX = screenX;
                touchDownY = screenY;
                if (creativeScroll < 0) creativeScroll = 0;
                if (creativeScroll > GameProc.MAX_CREATIVE_SCROLL)
                    creativeScroll = GameProc.MAX_CREATIVE_SCROLL;
            }
        }
    }

    public void scrolled(int amount) {
        switch (CaveGame.GAME_STATE) {
            case PLAY:
                GP.player.slot += amount;
                if (GP.player.slot < 0) GP.player.slot = 8;
                if (GP.player.slot > 8) GP.player.slot = 0;
                break;
            case CREATIVE_INV:
                creativeScroll += amount;
                if (creativeScroll < 0) creativeScroll = 0;
                if (creativeScroll > GameProc.MAX_CREATIVE_SCROLL)
                    creativeScroll = GameProc.MAX_CREATIVE_SCROLL;
                break;
        }
    }

    public int getKeyDownCode() {
        return keyDownCode;
    }

    public boolean isKeyDown() {
        return keyDown;
    }

    int getBlockDamage() {
        return blockDamage;
    }

    int getCurX() {
        return curX;
    }

    int getCurY() {
        return curY;
    }

    int getCreativeScroll() {
        return creativeScroll;
    }

    void update() {
        moveCursor();
        if (touchedDown && touchDownBtn == Input.Buttons.LEFT) {
            pressLMB();
        }
        if (touchedDown && TimeUtils.timeSinceMillis(touchDownTime) > 500) {
            holdMB();
        }
    }

}
