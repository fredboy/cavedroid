package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;
import com.google.common.collect.Range;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.GameScreen;
import ru.deadsoftware.cavedroid.game.mobs.FallingGravel;
import ru.deadsoftware.cavedroid.game.mobs.FallingSand;
import ru.deadsoftware.cavedroid.game.mobs.Mob;
import ru.deadsoftware.cavedroid.game.objects.Drop;
import ru.deadsoftware.cavedroid.game.objects.Player;
import ru.deadsoftware.cavedroid.misc.Assets;
import ru.deadsoftware.cavedroid.misc.ControlMode;
import ru.deadsoftware.cavedroid.misc.states.GameState;

import java.io.Serializable;
import java.util.ArrayList;

import static ru.deadsoftware.cavedroid.game.GameItems.*;

public class GameProc implements Serializable, Disposable {

    static final int MAX_CREATIVE_SCROLL = getItemsSize() / 8;

    private static final int WORLD_WIDTH = 1024;
    private static final int WORLD_HEIGHT = 256;
    private static final int UPD_RANGE = 16;

    static boolean DO_UPD = false;
    static int UPD_X = -1, UPD_Y = -1;

    private transient GameFluidsThread fluidThread;
    public transient GameWorld world;
    public transient GameRenderer renderer;
    transient GamePhysics physics;

    public ControlMode controlMode;
    public Player player;
    public ArrayList<Mob> mobs;
    ArrayList<Drop> drops;

    public boolean isKeyDown;
    public int keyDownCode;
    boolean isTouchDown;
    float touchDownX, touchDownY;
    int touchDownBtn;
    long touchDownTime;

    int curX, curY;
    int creativeScroll;
    int blockDmg = 0;

    public GameProc(int gameMode) {
        world = new GameWorld(WORLD_WIDTH, WORLD_HEIGHT);
        player = new Player(gameMode);
        drops = new ArrayList<>();
        mobs = new ArrayList<>();
        physics = new GamePhysics();
        controlMode = CaveGame.TOUCH ? ControlMode.WALK : ControlMode.CURSOR;
        resetRenderer();
        startFluidThread();
    }

    public void resetRenderer() {
        int scale = CaveGame.TOUCH ? 320 : 480;
        renderer = new GameRenderer(scale, scale * GameScreen.getHeight() / GameScreen.getWidth());
    }

    private void startFluidThread() {
        fluidThread = new GameFluidsThread();
        fluidThread.start();
    }

    private boolean isNotAutoselectable(int x, int y) {
        return (!world.hasForeAt(x, y) || !world.getForeMapBlock(x, y).hasCollision());
    }

    private void checkCursorBounds() {
        if (curY < 0) {
            curY = 0;
        } else if (curY >= world.getHeight()) {
            curY = world.getHeight() - 1;
        }

        if (controlMode == ControlMode.CURSOR) {
            if (curX * 16 + 8 < player.pos.x + player.getWidth() / 2) {
                player.setDir(0);
            } else {
                player.setDir(1);
            }
        }
    }

    private void moveCursor() {
        int pastX = curX;
        int pastY = curY;

        if (controlMode == ControlMode.WALK && CaveGame.TOUCH) {
            curX = player.getMapX() + (player.looksLeft() ? -1 : 1);
            curY = player.getUpperMapY();
            for (int i = 0; i < 2 && isNotAutoselectable(curX, curY); i++) {
                curY++;
            }
            if (isNotAutoselectable(curX, curY)) {
                curX += player.looksLeft() ? 1 : -1;
            }
        } else if (!CaveGame.TOUCH) {
            curX = (int) (Gdx.input.getX() * (renderer.getWidth() / GameScreen.getWidth()) + renderer.getCamX()) / 16;
            curY = (int) (Gdx.input.getY() * (renderer.getHeight() / GameScreen.getHeight()) + renderer.getCamY()) / 16;
            if (curX < 0) curX--;
        }

        if (pastX != curX || pastY != curY) {
            blockDmg = 0;
        }

        checkCursorBounds();
    }

    private void updateBlock(int x, int y) {
        if (world.getForeMap(x, y) == 10) {
            if (!world.hasForeAt(x, y + 1) || !world.getForeMapBlock(x, y + 1).hasCollision()) {
                world.setForeMap(x, y, 0);
                mobs.add(new FallingSand(x * 16, y * 16));
                updateBlock(x, y - 1);
            }
        }

        if (world.getForeMap(x, y) == 11) {
            if (!world.hasForeAt(x, y + 1) || !world.getForeMapBlock(x, y + 1).hasCollision()) {
                world.setForeMap(x, y, 0);
                mobs.add(new FallingGravel(x * 16, y * 16));
                updateBlock(x, y - 1);
            }
        }

        if (world.hasForeAt(x, y) && world.getForeMapBlock(x, y).requiresBlock()) {
            if (!world.hasForeAt(x, y + 1) || !world.getForeMapBlock(x, y + 1).hasCollision()) {
                world.destroyForeMap(x, y);
                updateBlock(x, y - 1);
            }
        }

        if (world.getForeMap(x, y) == 2) {
            if (world.hasForeAt(x, y - 1) && (world.getForeMapBlock(x, y - 1).hasCollision() ||
                    isFluid(world.getForeMap(x, y - 1)))) {
                world.setForeMap(x, y, 3);
            }
        }
    }

    private void blockUpdater() {
        if (DO_UPD) {
            for (int y = UPD_Y; y < UPD_Y + UPD_RANGE; y++) {
                for (int x = UPD_X; x < UPD_X + UPD_RANGE; x++) {
                    updateBlock(x, y);
                }
            }
            DO_UPD = false;
        }
    }

    void useItem(int x, int y, int id, boolean bg) {
        String key = getItem(id).isBlock() ? getBlockKey(id) : getItemKey(id);
        if (id > 0) {
            if (getItem(id).isBlock()) {
                if (!bg) {
                    world.placeToForeground(x, y, getBlockIdByItemId(id));
                } else {
                    world.placeToBackground(x, y, getBlockIdByItemId(id));
                }
            } else {
                switch (key) {
                    case "bucket_water":
                        world.placeToForeground(x, y, getBlockId("water"));
                        player.inventory[player.slot] = getItemId("bucket_empty");
                        break;
                    case "bucket_lava":
                        world.placeToForeground(x, y, getBlockId("lava"));
                        player.inventory[player.slot] = getItemId("bucket_empty");
                        break;
                }
            }
        }
    }

    private void pressLMB() {
        if ((world.hasForeAt(curX, curY) && world.getForeMapBlock(curX, curY).getHp() >= 0) ||
                (!world.hasForeAt(curX, curY) && world.hasBackAt(curX, curY) &&
                        world.getBackMapBlock(curX, curY).getHp() >= 0)) {
            if (player.gameMode == 0) {
                blockDmg++;
                if (world.hasForeAt(curX, curY)) {
                    if (blockDmg >= world.getForeMapBlock(curX, curY).getHp()) {
                        world.destroyForeMap(curX, curY);
                        blockDmg = 0;
                    }
                } else if (world.hasBackAt(curX, curY)) {
                    if (blockDmg >= world.getBackMapBlock(curX, curY).getHp()) {
                        world.destroyBackMap(curX, curY);
                        blockDmg = 0;
                    }
                }
            } else {
                if (world.hasForeAt(curX, curY)) {
                    world.placeToForeground(curX, curY, 0);
                } else if (world.hasBackAt(curX, curY)) {
                    world.placeToBackground(curX, curY, 0);
                }
                isTouchDown = false;
            }
        }
    }

    private boolean insideHotbar(float x, float y) {
        TextureRegion hotbar = Assets.textureRegions.get("hotbar");
        return y < hotbar.getRegionHeight() &&
                Range.open(renderer.getWidth() / 2 - (float) hotbar.getRegionWidth() / 2,
                        renderer.getWidth() / 2 + (float) hotbar.getRegionWidth() / 2).contains(x);
    }

    private void holdMB() {
        if (touchDownBtn == Input.Buttons.RIGHT) {
            useItem(curX, curY, player.inventory[player.slot], true);
            isTouchDown = false;
        } else {
            if (insideHotbar(touchDownX, touchDownY)) {
                CaveGame.GAME_STATE = GameState.CREATIVE_INV;
                isTouchDown = false;
            }
        }
    }

    public void update() {
        physics.update();
        blockUpdater();
        moveCursor();
        if (isTouchDown && touchDownBtn == Input.Buttons.LEFT) pressLMB();
        if (isTouchDown && TimeUtils.timeSinceMillis(touchDownTime) > 500) holdMB();
        if (fluidThread == null || !fluidThread.isAlive()) startFluidThread();
    }

    @Override
    public void dispose() {
        fluidThread.interrupt();
    }
}
