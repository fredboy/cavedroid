package ru.deadsoftware.cavedroid.game.world;

import com.badlogic.gdx.utils.Timer;
import ru.deadsoftware.cavedroid.game.GameItemsHolder;
import ru.deadsoftware.cavedroid.game.GameScope;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.model.block.Block;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import java.util.*;

@GameScope
public class GameWorldFluidsLogicControllerTask extends Timer.Task {

    public static final float FLUID_UPDATE_INTERVAL_SEC = 0.1f;

    private final GameWorld mGameWorld;
    private final MobsController mMobsController;
    private final GameItemsHolder mGameItemsHolder;

    private final Map<Class<? extends Block.Fluid>, List<? extends Block.Fluid>> mFluidStatesMap;

    @Inject
    GameWorldFluidsLogicControllerTask(GameWorld gameWorld,
                                       MobsController mobsController,
                                       GameItemsHolder gameItemsHolder) {
        mGameWorld = gameWorld;
        mMobsController = mobsController;
        mGameItemsHolder = gameItemsHolder;

        final List<Block.Water> waters = mGameItemsHolder.getBlocksByType(Block.Water.class);
        waters.sort(Comparator.comparingInt(Block.Water::getState));

        final List<Block.Lava> lavas = mGameItemsHolder.getBlocksByType(Block.Lava.class);
        lavas.sort(Comparator.comparingInt(Block.Lava::getState));

        mFluidStatesMap = new HashMap<>();
        mFluidStatesMap.put(Block.Water.class, waters);
        mFluidStatesMap.put(Block.Lava.class, lavas);
    }

    @CheckForNull
    private List<? extends Block.Fluid> getFluidStateList(Block.Fluid fluid) {
        return mFluidStatesMap.get(fluid.getClass());
    }

    private int getCurrentStateIndex(Block.Fluid fluid) {
        @CheckForNull final List<? extends Block.Fluid> stateList = getFluidStateList(fluid);

        if (stateList == null) {
            return -1;
        }

        return stateList.indexOf(fluid);
    }

    @CheckForNull
    private Block.Fluid getNextStateBlock(Block.Fluid fluid) {
        @CheckForNull final List<? extends Block.Fluid> stateList = getFluidStateList(fluid);

        if (stateList == null) {
            return null;
        }

        int currentState = stateList.indexOf(fluid);

        if (currentState < 0) {
            return null;
        }

        int nextState = currentState + 1;

        if (nextState == 1) {
            nextState++;
        }

        if (nextState < stateList.size()) {
            return stateList.get(nextState);
        }

        return null;
    }

    private boolean noFluidNearby(int x, int y) {
        return !mGameWorld.getForeMap(x, y - 1).isFluid() &&
                (!mGameWorld.getForeMap(x - 1, y).isFluid() || ((Block.Fluid)mGameWorld.getForeMap(x - 1, y)).getState() >= ((Block.Fluid)mGameWorld.getForeMap(x, y)).getState()) &&
                (!mGameWorld.getForeMap(x + 1, y).isFluid() || ((Block.Fluid)mGameWorld.getForeMap(x + 1, y)).getState() >= ((Block.Fluid)mGameWorld.getForeMap(x, y)).getState());
    }

    private boolean drainFluid(int x, int y) {
        final Block block = mGameWorld.getForeMap(x, y);

        if (!(block instanceof Block.Fluid fluid)) {
            return true;
        }

        if (fluid.getState() > 0) {
            if (noFluidNearby(x, y)) {
                @CheckForNull final Block nextState = getNextStateBlock(fluid);
                if (nextState == null) {
                    mGameWorld.resetForeMap(x, y);
                    return true;
                }

                mGameWorld.setForeMap(x, y, nextState);
            }
        }
        return false;
    }

    private boolean fluidCanFlowThere(Block.Fluid fluid, Block targetBlock) {
        return targetBlock == mGameItemsHolder.getFallbackBlock() ||
                (!targetBlock.getParams().getHasCollision() && !targetBlock.isFluid()) ||
                (fluid.getClass() == targetBlock.getClass() && fluid.getState() < ((Block.Fluid)targetBlock).getState());
    }

    private void flowFluidTo(Block.Fluid currentFluid, int x, int y, Block.Fluid nextStateFluid) {
        final Block targetBlock = mGameWorld.getForeMap(x, y);

        if (fluidCanFlowThere(currentFluid, targetBlock)) {
            mGameWorld.setForeMap(x, y, nextStateFluid);
        } else if (currentFluid.isWater() && targetBlock.isLava()) {
            if (((Block.Lava)targetBlock).getState() > 0) {
                mGameWorld.setForeMap(x, y, mGameItemsHolder.getBlock("cobblestone"));
            } else {
                mGameWorld.setForeMap(x, y, mGameItemsHolder.getBlock("obsidian"));
            }
        } else if (currentFluid.isLava() && targetBlock.isWater()) {
            mGameWorld.setForeMap(x, y, mGameItemsHolder.getBlock("stone"));
        }
    }

    private void flowFluid(int x, int y) {
        Block.Fluid fluid = (Block.Fluid) mGameWorld.getForeMap(x, y);
        @CheckForNull final List<? extends Block.Fluid> stateList = getFluidStateList(fluid);

        if (stateList == null) {
            return;
        }

        if (fluid.getState() < stateList.size() - 1 && mGameWorld.getForeMap(x, y + 1).hasCollision()) {
            @CheckForNull Block.Fluid nextState = getNextStateBlock(fluid);

            if (nextState == null) {
                return;
            }

            flowFluidTo(fluid, x - 1, y, nextState);
            flowFluidTo(fluid, x + 1, y, nextState);
        } else {
            flowFluidTo(fluid, x, y + 1, stateList.get(1));
        }

    }

    private void updateFluids(int x, int y) {
        if (!mGameWorld.getForeMap(x, y).isFluid()) {
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

    @Override
    public void run() {
        fluidUpdater();
    }
}
