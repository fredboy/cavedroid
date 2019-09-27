package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.GameScreen;
import ru.deadsoftware.cavedroid.game.mobs.FallingGravel;
import ru.deadsoftware.cavedroid.game.mobs.FallingSand;
import ru.deadsoftware.cavedroid.game.mobs.Mob;
import ru.deadsoftware.cavedroid.game.mobs.Pig;
import ru.deadsoftware.cavedroid.game.objects.Drop;
import ru.deadsoftware.cavedroid.game.objects.Player;
import ru.deadsoftware.cavedroid.misc.AppState;
import ru.deadsoftware.cavedroid.misc.Assets;

import java.io.Serializable;
import java.util.ArrayList;

public class GameProc implements Serializable, Disposable {
    static boolean DO_UPD = false;
    static int UPD_X = -1, UPD_Y = -1;

    private transient GameFluidsThread fluidThread;

    public transient GameWorld world;
    public transient GameRenderer renderer;
    public transient GamePhysics physics;

    public Player player;
    public ArrayList<Mob> mobs;
    public ArrayList<Drop> drops;

    public boolean isTouchDown, isKeyDown;
    public int ctrlMode, touchDownX, touchDownY, touchDownBtn, keyDownCode;
    public long touchDownTime;

    int curX, curY;
    int creativeScroll, maxCreativeScroll;
    int blockDmg = 0;

    public void initGame(int gameMode) {
        world = new GameWorld();
        world.generate(1024, 256);
        player = new Player(gameMode);
        drops = new ArrayList<>();
        mobs = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            mobs.add(new Pig(i * 256, 196 * 16));
        }
        physics = new GamePhysics();
        if (CaveGame.TOUCH) {
            renderer = new GameRenderer(320,
                    320 * ((float) GameScreen.getHeight() / GameScreen.getWidth()));
        } else {
            ctrlMode = 1;
            renderer = new GameRenderer(480,
                    480 * ((float) GameScreen.getHeight() / GameScreen.getWidth()));
        }
        maxCreativeScroll = GameItems.getItemsSize() / 8;

        createFluidThread();
        fluidThread.start();

        GameSaver.save(this);
    }

    private void createFluidThread() {
        fluidThread = new GameFluidsThread();
    }

    public void resetRenderer() {
        if (CaveGame.TOUCH) {
            renderer = new GameRenderer(320,
                    320 * ((float) GameScreen.getHeight() / GameScreen.getWidth()));
        } else {
            renderer = new GameRenderer(480,
                    480 * ((float) GameScreen.getHeight() / GameScreen.getWidth()));
        }
    }

    private boolean isAutoselectable(int x, int y) {
        return (world.getForeMap(x, y) > 0 &&
                GameItems.getBlock(world.getForeMap(x, y)).hasCollision());
    }

    private void moveCursor() {
        int pastX = curX, pastY = curY;
        if (ctrlMode == 0 && CaveGame.TOUCH) {
            curX = player.getMapX();
            if (player.getDir() == 0) curX--;
            else curX++;
            curY = (int) (player.pos.y + player.getWidth()) / 16;
            if (!isAutoselectable(curX, curY)) {
                curY++;
            }
            if (!isAutoselectable(curX, curY)) {
                curY++;
            }
            if (!isAutoselectable(curX, curY)) {
                if (player.getDir() == 0) curX++;
                else curX--;
            }
        } else if (!CaveGame.TOUCH) {
            curX = (int) (Gdx.input.getX() *
                    (renderer.getWidth() / GameScreen.getWidth()) + renderer.getCamX()) / 16;
            curY = (int) (Gdx.input.getY() *
                    (renderer.getHeight() / GameScreen.getHeight()) + renderer.getCamY()) / 16;
            if ((Gdx.input.getX() *
                    (renderer.getWidth() / GameScreen.getWidth()) + renderer.getCamX()) < 0)
                curX--;
        }
        if (pastX != curX || pastY != curY) blockDmg = 0;
    }

    private void checkCursorBounds() {
        if (curY < 0) curY = 0;
        if (curY >= world.getHeight()) curY = world.getHeight() - 1;
        if (ctrlMode == 1) {
            if (curX * 16 + 8 < player.pos.x + player.getWidth() / 2)
                player.setDir(0);
            if (curX * 16 + 8 > player.pos.x + player.getWidth() / 2)
                player.setDir(1);
        }
    }

    private void updateBlock(int x, int y) {
        if (world.getForeMap(x, y) == 10) {
            if (world.getForeMap(x, y + 1) == 0 || !GameItems.getBlock(world.getForeMap(x, y + 1)).hasCollision()) {
                world.setForeMap(x, y, 0);
                mobs.add(new FallingSand(x * 16, y * 16));
                updateBlock(x, y - 1);
            }
        }

        if (world.getForeMap(x, y) == 11) {
            if (world.getForeMap(x, y + 1) == 0 || !GameItems.getBlock(world.getForeMap(x, y + 1)).hasCollision()) {
                world.setForeMap(x, y, 0);
                mobs.add(new FallingGravel(x * 16, y * 16));
                updateBlock(x, y - 1);
            }
        }

        if (world.getForeMap(x, y) > 0 && GameItems.getBlock(world.getForeMap(x, y)).requiresBlock()) {
            if (world.getForeMap(x, y + 1) == 0 || !GameItems.getBlock(world.getForeMap(x, y + 1)).hasCollision()) {
                world.destroyForeMap(x, y);
                updateBlock(x, y - 1);
            }
        }

        if (world.getForeMap(x, y) == 2) {
            if (world.getForeMap(x, y - 1) > 0 && (GameItems.getBlock(world.getForeMap(x, y - 1)).hasCollision() ||
                    GameItems.isFluid(world.getForeMap(x, y - 1)))) {
                world.setForeMap(x, y, 3);
            }
        }
    }

    void useItem(int x, int y, int id, boolean bg) {
        if (id > 0) {
            if (GameItems.getItem(id).isBlock()) {
                if (!bg) world.placeToForeground(x, y, GameItems.getBlockIdByItemId(id));
                else world.placeToBackground(x, y, GameItems.getBlockIdByItemId(id));
            } else {
                switch (id) {
                    case 65:
                        world.placeToForeground(x, y, 8);
                        player.inv[player.invSlot] = 64;
                        break;
                    case 66:
                        world.placeToForeground(x, y, 9);
                        player.inv[player.invSlot] = 64;
                        break;
                }
            }
        }
    }

    public void update(float delta) {
        if (DO_UPD) {
            for (int y = UPD_Y; y < UPD_Y + 16; y++)
                for (int x = UPD_X; x < UPD_X + 16; x++) {
                    updateBlock(x, y);
                }
            DO_UPD = false;
        }

        physics.update(delta);
        moveCursor();
        checkCursorBounds();

        if (isTouchDown && touchDownBtn == Input.Buttons.LEFT) {
            if ((world.getForeMap(curX, curY) > 0 && GameItems.getBlock(world.getForeMap(curX, curY)).getHp() >= 0) ||
                    (world.getForeMap(curX, curY) == 0 &&
                            world.getBackMap(curX, curY) > 0 &&
                            GameItems.getBlock(world.getBackMap(curX, curY)).getHp() >= 0)) {
                if (player.gameMode == 0) {
                    blockDmg++;
                    if (world.getForeMap(curX, curY) > 0) {
                        if (blockDmg >= GameItems.getBlock(world.getForeMap(curX, curY)).getHp()) {
                            world.destroyForeMap(curX, curY);
                            blockDmg = 0;
                        }
                    } else if (world.getBackMap(curX, curY) > 0) {
                        if (blockDmg >= GameItems.getBlock(world.getBackMap(curX, curY)).getHp()) {
                            world.destroyBackMap(curX, curY);
                            blockDmg = 0;
                        }
                    }
                } else {
                    if (world.getForeMap(curX, curY) > 0) world.placeToForeground(curX, curY, 0);
                    else if (world.getBackMap(curX, curY) > 0) world.placeToBackground(curX, curY, 0);
                    isTouchDown = false;
                }
            }
        }

        if (isTouchDown && TimeUtils.timeSinceMillis(touchDownTime) > 500) {
            if (touchDownBtn == Input.Buttons.RIGHT) {
                useItem(curX, curY, player.inv[player.invSlot], true);
                isTouchDown = false;
            } else if (touchDownY < Assets.invBar.getRegionHeight() &&
                    touchDownX > renderer.getWidth() / 2 - (float) Assets.invBar.getRegionWidth() / 2 &&
                    touchDownX < renderer.getWidth() / 2 + (float) Assets.invBar.getRegionWidth() / 2) {
                CaveGame.STATE = AppState.GAME_CREATIVE_INV;
                isTouchDown = false;
            }
        }

        if (fluidThread == null || !fluidThread.isAlive()) {
            createFluidThread();
            fluidThread.start();
        }
    }

    @Override
    public void dispose() {
        fluidThread.interrupt();
    }
}
