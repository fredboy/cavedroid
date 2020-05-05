package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.utils.TimeUtils;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;

import java.util.Arrays;

import static ru.deadsoftware.cavedroid.game.GameItems.*;

class GameFluidsThread extends Thread {

    private static final int FLUID_UPDATE_INTERVAL_MS = 100;
    private static final int FLUID_STATES = 5;

    private static final int[] WATER_IDS = {8, 60, 61, 62, 63};
    private static final int[] LAVA_IDS = {9, 64, 65, 66, 67};

    private long mFluidLastUpdateTimestamp = 0;
    
    private final GameWorld mGameWorld;
    private final MobsController mMobsController;

    private final Thread mMainThread;
    
    GameFluidsThread(GameWorld gameWorld,
                     MobsController mobsController,
                     Thread mainThread) {
        mGameWorld = gameWorld;
        mMobsController = mobsController;
        mMainThread = mainThread;
    }

    private int getBlockState(int id) {
        return isWater(id) ? Arrays.binarySearch(WATER_IDS, id) : Arrays.binarySearch(LAVA_IDS, id);
    }

    private int getNextBlockState(int id) {
        if (!isFluid(id)) {
            return -1;
        }
        int state = getBlockState(id);
        if (state < FLUID_STATES - 1) {
            return state + 1;
        }
        return -1;
    }

    private int getNextBlockStateId(int id) {
        int nextState = getNextBlockState(id);
        if (nextState == -1) {
            return 0;
        }
        if (isWater(id)) {
            return WATER_IDS[nextState];
        }
        return LAVA_IDS[nextState];
    }

    private int id(int x, int y) {
        return mGameWorld.getForeMap(x, y);
    }

    private boolean sameFluid(int thisId, int thatId) {
        return isFluid(thatId) && isWater(thatId) == isWater(thisId);
    }

    private boolean noFluidNearby(int x, int y) {
        return !isFluid(id(x, y - 1)) &&
                (!isFluid(id(x - 1, y)) || id(x - 1, y) >= id(x, y)) &&
                (!isFluid(id(x + 1, y)) || id(x + 1, y) >= id(x, y));
    }

    private boolean drainFluid(int x, int y) {
        if (getBlockState(id(x, y)) > 0) {
            if (noFluidNearby(x, y)) {
                mGameWorld.setForeMap(x, y, getNextBlockStateId(id(x, y)));
            }
            if (!isFluid(id(x, y))) {
                mGameWorld.setForeMap(x, y, 0);
                return true;
            }
        }
        return false;
    }

    private void flowFluidTo(int thisId, int x, int y, int nextStateId) {
        int thatId = id(x, y);
        if (fluidCanFlowThere(thisId, thatId)) {
            mGameWorld.setForeMap(x, y, nextStateId);
        } else if (isWater(thisId) && isLava(thatId)) {
            if (getBlockState(thatId) > 0) {
                mGameWorld.setForeMap(x, y, 4); //cobblestone
            } else {
                mGameWorld.setForeMap(x, y, 68); //obsidian
            }
        } else if (isLava(thisId) && isWater(thatId)) {
            mGameWorld.setForeMap(x, y, 1); //stone
        }
    }

    private void flowFluid(int x, int y) {
        int id = id(x, y);
        if (getBlockState(id) < FLUID_STATES - 1 && getBlock(id(x, y + 1)).hasCollision()) {
            int nextState = getNextBlockState(id);
            int nextStateId = getNextBlockStateId(id);
            if (nextState == 1) {
                nextStateId++;
            }
            flowFluidTo(id, x - 1, y, nextStateId);
            flowFluidTo(id, x + 1, y, nextStateId);
        } else {
            flowFluidTo(id, x, y + 1, isWater(id) ? WATER_IDS[1] : LAVA_IDS[1]);
        }

    }

    private void updateFluids(int x, int y) {
        if (!isFluid(id(x, y))) {
            return;
        }
        if (drainFluid(x, y)) {
            return;
        }
        flowFluid(x, y);
    }

    private void fluidUpdater() {
        int midScreen = (int) mMobsController.getPlayer().x / 16;
        for (int y = mGameWorld.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x <= mGameWorld.getWidth() / 2; x++) {
                updateFluids(midScreen + x, y);
                updateFluids(midScreen - x, y);
            }
        }
    }

    private boolean timeToUpdate() {
        if (TimeUtils.timeSinceMillis(mFluidLastUpdateTimestamp) >= FLUID_UPDATE_INTERVAL_MS) {
            mFluidLastUpdateTimestamp = TimeUtils.millis();
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        while (!this.isInterrupted() && mMainThread.isAlive()) {
            if (timeToUpdate()) {
                fluidUpdater();
            }
        }
    }
}
