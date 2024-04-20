package ru.deadsoftware.cavedroid.game.world;

import kotlin.Pair;
import ru.deadsoftware.cavedroid.game.GameItems;
import ru.deadsoftware.cavedroid.game.GameItemsHolder;
import ru.deadsoftware.cavedroid.game.GameScope;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.model.block.Block;
import ru.deadsoftware.cavedroid.game.model.world.generator.WorldGeneratorConfig;
import ru.deadsoftware.cavedroid.game.objects.DropController;

import javax.annotation.CheckForNull;
import javax.inject.Inject;

@GameScope
public class GameWorld {

    private final DropController mDropController;
    private final MobsController mMobsController;
    private final GameItemsHolder mGameItemsHolder;

    private final int mWidth;
    private final int mHeight;
    private final Block[][] mForeMap;
    private final Block[][] mBackMap;

    @Inject
    public GameWorld(DropController dropController,
                     MobsController mobsController,
                     GameItemsHolder gameItemsHolder,
                     @CheckForNull Block[][] foreMap,
                     @CheckForNull Block[][] backMap) {
        mDropController = dropController;
        mMobsController = mobsController;
        mGameItemsHolder = gameItemsHolder;

        boolean isNewGame = foreMap == null || backMap == null;

        if (isNewGame) {
            final WorldGeneratorConfig config = WorldGeneratorConfig.Companion.getDefault();
            mWidth = config.getWidth();
            mHeight = config.getHeight();
            Pair<Block[][], Block[][]> maps = new GameWorldGenerator(config, mGameItemsHolder).generate();
            mForeMap = maps.getFirst();
            mBackMap = maps.getSecond();
            mMobsController.getPlayer().respawn(this, mGameItemsHolder);
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

    public Block[][] getFullForeMap() {
        return mForeMap;
    }

    public Block[][] getFullBackMap() {
        return mBackMap;
    }

    private int transformX(int x) {
        x = x % getWidth();
        if (x < 0) {
            x = getWidth() - Math.abs(x);
        }
        return x;
    }

    private Block getMap(int x, int y, int layer) {
        Block map = mGameItemsHolder.getFallbackBlock();
        try {
            x = transformX(x);
            map = (layer == 0) ? mForeMap[x][y] : mBackMap[x][y];
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        return map;
    }

    private void setMap(int x, int y, int layer, Block value) {
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
        return getMap(x, y, 0) != mGameItemsHolder.getFallbackBlock();
    }

    public boolean hasBackAt(int x, int y) {
        return getMap(x, y, 1) != mGameItemsHolder.getFallbackBlock();
    }

    public Block getForeMap(int x, int y) {
        return getMap(x, y, 0);
    }

    public void setForeMap(int x, int y, Block block) {
        setMap(x, y, 0, block);
    }

    public void resetForeMap(int x, int y) {
        setForeMap(x, y, mGameItemsHolder.getFallbackBlock());
    }

    public Block getBackMap(int x, int y) {
        return getMap(x, y, 1);
    }

    public void setBackMap(int x, int y, Block block) {
        setMap(x, y, 1, block);
    }

    public void placeToForeground(int x, int y, Block value) {
        if (!hasForeAt(x, y) || value == mGameItemsHolder.getFallbackBlock() || !getForeMap(x, y).hasCollision()) {
            setForeMap(x, y, value);
        } else if (value instanceof Block.Slab && getForeMap(x, y) == value) {
            setForeMap(x, y, mGameItemsHolder.getBlock(((Block.Slab) value).getFullBlockKey()));
        }
    }

    public void placeToBackground(int x, int y, Block value) {
        if (value == mGameItemsHolder.getFallbackBlock() || (getBackMap(x, y) == mGameItemsHolder.getFallbackBlock() && value.hasCollision()) &&
                (!value.isTransparent() || value == mGameItemsHolder.getBlock("glass"))) {
            setBackMap(x, y, value);
        }
    }

    public void destroyForeMap(int x, int y) {
        Block block = getForeMap(x, y);
        if (block.hasDrop()) {
            mDropController.addDrop(transformX(x) * 16 + 4, y * 16 + 4, GameItems.getItemId(block.getDrop()));
        }
        placeToForeground(x, y, mGameItemsHolder.getFallbackBlock());
    }

    public void destroyBackMap(int x, int y) {
        Block block = getBackMap(x, y);
        if (block.hasDrop()) {
            mDropController.addDrop(transformX(x) * 16 + 4, y * 16 + 4, GameItems.getItemId(block.getDrop()));
        }
        placeToBackground(x, y, mGameItemsHolder.getFallbackBlock());
    }
}