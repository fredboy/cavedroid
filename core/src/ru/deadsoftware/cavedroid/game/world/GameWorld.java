package ru.deadsoftware.cavedroid.game.world;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Timer;
import kotlin.Pair;
import ru.deadsoftware.cavedroid.game.GameItems;
import ru.deadsoftware.cavedroid.game.GameScope;
import ru.deadsoftware.cavedroid.game.mobs.FallingGravel;
import ru.deadsoftware.cavedroid.game.mobs.FallingSand;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.model.world.generator.WorldGeneratorConfig;
import ru.deadsoftware.cavedroid.game.objects.Block;
import ru.deadsoftware.cavedroid.game.objects.DropController;

import javax.annotation.CheckForNull;
import javax.inject.Inject;

@GameScope
public class GameWorld implements Disposable {

    private static final int UPDATE_RANGE = 16;

    private final DropController mDropController;
    private final MobsController mMobsController;

    private final Timer mGameFluidsTimer;
    private final GameFluidsThread mGameFluidsThread;

    private final int mWidth;
    private final int mHeight;
    private final int[][] mForeMap;
    private final int[][] mBackMap;

    private boolean mShouldUpdate;
    private int mUpdateX;
    private int mUpdateY;

    @Inject
    public GameWorld(DropController dropController,
                     MobsController mobsController,
                     @CheckForNull int[][] foreMap,
                     @CheckForNull int[][] backMap) {
        mDropController = dropController;
        mMobsController = mobsController;

        boolean isNewGame = foreMap == null || backMap == null;

        if (isNewGame) {
            final WorldGeneratorConfig config = WorldGeneratorConfig.Companion.getDefault();
            mWidth = config.getWidth();
            mHeight = config.getHeight();
            Pair<int[][], int[][]> maps = new GameWorldGenerator(config).generate();
            mForeMap = maps.getFirst();
            mBackMap = maps.getSecond();
            mMobsController.getPlayer().respawn(this);
        } else {
            mForeMap = foreMap;
            mBackMap = backMap;
            mWidth = mForeMap.length;
            mHeight = mForeMap[0].length;
        }

        mGameFluidsThread = new GameFluidsThread(this, mMobsController);

        mGameFluidsTimer = new Timer();
        mGameFluidsTimer.scheduleTask(mGameFluidsThread, 0, GameFluidsThread.FLUID_UPDATE_INTERVAL_SEC);
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public float getWidthPx() {
        return mWidth * 16f;
    }

    public float getHeightPx() {
        return mHeight * 16f;
    }

    public int[][] getFullForeMap() {
        return mForeMap;
    }

    public int[][] getFullBackMap() {
        return mBackMap;
    }

    private int transformX(int x) {
        x = x % getWidth();
        if (x < 0) {
            x = getWidth() - Math.abs(x);
        }
        return x;
    }

    private int getMap(int x, int y, int layer) {
        int map = 0;
        try {
            x = transformX(x);
            map = (layer == 0) ? mForeMap[x][y] : mBackMap[x][y];
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        return map;
    }

    private void setMap(int x, int y, int layer, int value) {
        try {
            x = transformX(x);
            if (layer == 0) {
                mForeMap[x][y] = value;
            } else {
                mBackMap[x][y] = value;
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
    }

    public boolean hasForeAt(int x, int y) {
        return getMap(x, y, 0) != 0;
    }

    public boolean hasBackAt(int x, int y) {
        return getMap(x, y, 1) != 0;
    }

    public int getForeMap(int x, int y) {
        return getMap(x, y, 0);
    }

    public Block getForeMapBlock(int x, int y) {
        return GameItems.getBlock(getMap(x, y, 0));
    }

    public void setForeMap(int x, int y, int id) {
        setMap(x, y, 0, id);
    }

    public int getBackMap(int x, int y) {
        return getMap(x, y, 1);
    }

    public Block getBackMapBlock(int x, int y) {
        return GameItems.getBlock(getMap(x, y, 1));
    }

    public void setBackMap(int x, int y, int id) {
        setMap(x, y, 1, id);
    }

    private void placeSlab(int x, int y, int value) {
        switch (value) {
            case 51:
                setForeMap(x, y, 52);
                break;
            case 53:
                setForeMap(x, y, 21);
                break;
            case 54:
                setForeMap(x, y, 5);
                break;
            case 55:
                setForeMap(x, y, 4);
                break;
            case 56:
                setForeMap(x, y, 28);
                break;
            case 58:
                setForeMap(x, y, 57);
                break;
        }
    }

    public void placeToForeground(int x, int y, int value) {
        if (!hasForeAt(x, y) || value == 0 || !GameItems.getBlock(getForeMap(x, y)).hasCollision()) {
            setForeMap(x, y, value);
        } else if (GameItems.isSlab(value) && getForeMap(x, y) == value) {
            placeSlab(x, y, value);
        }
        mUpdateX = x - 8;
        mUpdateY = y - 8;
        mShouldUpdate = true;
    }

    public void placeToBackground(int x, int y, int value) {
        if (value == 0 || (getBackMap(x, y) == 0 && GameItems.getBlock(value).hasCollision()) &&
                (!GameItems.getBlock(value).isTransparent() || value == 18)) {
            setBackMap(x, y, value);
        }
    }

    public void destroyForeMap(int x, int y) {
        Block block = GameItems.getBlock(getForeMap(x, y));
        if (block.hasDrop()) {
            mDropController.addDrop(transformX(x) * 16 + 4, y * 16 + 4, GameItems.getItemId(block.getDrop()));
        }
        placeToForeground(x, y, 0);
    }

    public void destroyBackMap(int x, int y) {
        Block block = GameItems.getBlock(getBackMap(x, y));
        if (block.hasDrop()) {
            mDropController.addDrop(transformX(x) * 16 + 4, y * 16 + 4, GameItems.getItemId(block.getDrop()));
        }
        placeToBackground(x, y, 0);
    }

    private void updateBlock(int x, int y) {
        if (getForeMap(x, y) == 10) {
            if (!hasForeAt(x, y + 1) || !getForeMapBlock(x, y + 1).hasCollision()) {
                setForeMap(x, y, 0);
                mMobsController.addMob(new FallingSand(x * 16, y * 16));
                updateBlock(x, y - 1);
            }
        }

        if (getForeMap(x, y) == 11) {
            if (!hasForeAt(x, y + 1) || !getForeMapBlock(x, y + 1).hasCollision()) {
                setForeMap(x, y, 0);
                mMobsController.addMob(new FallingGravel(x * 16, y * 16));
                updateBlock(x, y - 1);
            }
        }

        if (hasForeAt(x, y) && getForeMapBlock(x, y).requiresBlock()) {
            if (!hasForeAt(x, y + 1) || !getForeMapBlock(x, y + 1).hasCollision()) {
                destroyForeMap(x, y);
                updateBlock(x, y - 1);
            }
        }

        if (getForeMap(x, y) == 2) {
            if (hasForeAt(x, y - 1) && (getForeMapBlock(x, y - 1).hasCollision() ||
                    GameItems.isFluid(getForeMap(x, y - 1)))) {
                setForeMap(x, y, 3);
            }
        }
    }

    public void update() {
        if (mShouldUpdate) {
            for (int y = mUpdateY; y < mUpdateY + UPDATE_RANGE; y++) {
                for (int x = mUpdateX; x < mUpdateX + UPDATE_RANGE; x++) {
                    updateBlock(x, y);
                }
            }
            mShouldUpdate = false;
        }
    }

    @Override
    public void dispose() {
        mGameFluidsThread.cancel();
    }
}