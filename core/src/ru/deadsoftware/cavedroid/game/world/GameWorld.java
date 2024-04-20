package ru.deadsoftware.cavedroid.game.world;

import kotlin.Pair;
import ru.deadsoftware.cavedroid.game.GameItems;
import ru.deadsoftware.cavedroid.game.GameScope;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.model.world.generator.WorldGeneratorConfig;
import ru.deadsoftware.cavedroid.game.objects.Block;
import ru.deadsoftware.cavedroid.game.objects.DropController;

import javax.annotation.CheckForNull;
import javax.inject.Inject;

@GameScope
public class GameWorld {

    private final DropController mDropController;
    private final MobsController mMobsController;

    private final int mWidth;
    private final int mHeight;
    private final int[][] mForeMap;
    private final int[][] mBackMap;

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

    public void placeToForeground(int x, int y, int value) {
        if (!hasForeAt(x, y) || value == 0 || !GameItems.getBlock(getForeMap(x, y)).hasCollision()) {
            setForeMap(x, y, value);
        } else if (GameItems.isSlab(value) && getForeMap(x, y) == value) {
            final Block block = GameItems.getBlock(value);
            if (block.getFullBlockKey() != null) {
                setForeMap(x, y, GameItems.getBlockId(block.getFullBlockKey()));
            }
        }
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
}