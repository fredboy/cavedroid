package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.GameScreen;
import ru.deadsoftware.cavedroid.game.mobs.FallingGravel;
import ru.deadsoftware.cavedroid.game.mobs.FallingSand;
import ru.deadsoftware.cavedroid.game.mobs.Mob;
import ru.deadsoftware.cavedroid.game.objects.Drop;
import ru.deadsoftware.cavedroid.game.objects.Player;
import ru.deadsoftware.cavedroid.misc.ControlMode;

import java.io.Serializable;
import java.util.ArrayList;

public class GameProc implements Serializable, Disposable {

    static final int MAX_CREATIVE_SCROLL = GameItems.getItemsSize() / 8;

    private static final int WORLD_WIDTH = 1024;
    private static final int WORLD_HEIGHT = 256;
    private static final int UPD_RANGE = 16;

    static boolean DO_UPD = false;
    static int UPD_X = -1, UPD_Y = -1;

    private transient GameFluidsThread fluidThread;
    public transient GameWorld world;
    public transient GameRenderer renderer;
    public transient GameInput input;
    transient GamePhysics physics;

    public ControlMode controlMode;
    public Player player;
    public ArrayList<Mob> mobs;
    ArrayList<Drop> drops;

    public void resetRenderer() {
        int scale = CaveGame.TOUCH ? 320 : 480;
        renderer = new GameRenderer(scale, scale * GameScreen.getHeight() / GameScreen.getWidth());
    }

    public GameProc(int gameMode) {
        world = new GameWorld(WORLD_WIDTH, WORLD_HEIGHT);
        player = new Player(gameMode);
        drops = new ArrayList<>();
        mobs = new ArrayList<>();
        physics = new GamePhysics();
        input = new GameInput();
        controlMode = CaveGame.TOUCH ? ControlMode.WALK : ControlMode.CURSOR;
        resetRenderer();
        startFluidThread();
    }

    private void startFluidThread() {
        fluidThread = new GameFluidsThread();
        fluidThread.start();
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
                    GameItems.isFluid(world.getForeMap(x, y - 1)))) {
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

    public void update() {
        physics.update();
        input.update();
        blockUpdater();
        if (fluidThread == null || !fluidThread.isAlive()) startFluidThread();
    }

    @Override
    public void dispose() {
        fluidThread.interrupt();
    }
}
