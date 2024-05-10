package ru.deadsoftware.cavedroid.game.world;

import kotlin.Pair;
import ru.deadsoftware.cavedroid.game.GameItemsHolder;
import ru.deadsoftware.cavedroid.game.GameScope;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.model.block.Block;
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem;
import ru.deadsoftware.cavedroid.game.model.item.Item;
import ru.deadsoftware.cavedroid.game.model.world.generator.WorldGeneratorConfig;
import ru.deadsoftware.cavedroid.game.objects.container.Container;
import ru.deadsoftware.cavedroid.game.objects.drop.DropController;
import ru.deadsoftware.cavedroid.game.objects.container.Furnace;
import ru.deadsoftware.cavedroid.game.objects.container.ContainerController;
import ru.deadsoftware.cavedroid.misc.utils.MeasureUnitsUtilsKt;

import javax.annotation.CheckForNull;
import javax.inject.Inject;

@GameScope
public class GameWorld {

    private static final int FOREGROUND_Z = 0;
    private static final int BACKGROUND_Z = 1;

    private final DropController mDropController;
    private final MobsController mMobsController;
    private final GameItemsHolder mGameItemsHolder;
    private final ContainerController mContainerController;

    private final int mWidth;
    private final int mHeight;
    private final Block[][] mForeMap;
    private final Block[][] mBackMap;

    private final WorldGeneratorConfig mWorldConfig = WorldGeneratorConfig.Companion.getDefault();

    @Inject
    public GameWorld(DropController dropController,
                     MobsController mobsController,
                     GameItemsHolder gameItemsHolder,
                     ContainerController containerController,
                     @CheckForNull Block[][] foreMap,
                     @CheckForNull Block[][] backMap) {
        mDropController = dropController;
        mMobsController = mobsController;
        mGameItemsHolder = gameItemsHolder;
        mContainerController = containerController;

        boolean isNewGame = foreMap == null || backMap == null;

        if (isNewGame) {
            mWidth = mWorldConfig.getWidth();
            mHeight = mWorldConfig.getHeight();
            Pair<Block[][], Block[][]> maps = new GameWorldGenerator(mWorldConfig, mGameItemsHolder).generate();
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

    /**
     * @deprecated for kotlin use {@link MeasureUnitsUtilsKt#getPx } extension val
     */
    @Deprecated
    public float getWidthPx() {
        return MeasureUnitsUtilsKt.getPx(mWidth);
    }

    /**
     * @deprecated for kotlin use {@link MeasureUnitsUtilsKt#getPx } extension val
     */
    @Deprecated
    public float getHeightPx() {
        return MeasureUnitsUtilsKt.getPx(mHeight);
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

        if (y < 0 || y >= getHeight()) {
            return map;
        }

        x = transformX(x);

        if (x < 0 || x >= getWidth()) {
            return map;
        }

        map = (layer == 0) ? mForeMap[x][y] : mBackMap[x][y];

        return map;
    }

    private void setMap(int x, int y, int layer, Block value) {
        if (y < 0 || y >= getHeight()) {
            return;
        }

        x = transformX(x);

        if (x < 0 || x >= getWidth()) {
            return;
        }

        mContainerController.destroyContainer(x, y, layer, false);

        if (value.isContainer()) {
            mContainerController.addContainer(x, y, layer, (Class<? extends Block.Container>) value.getClass());
        }

        if (layer == 0) {
            mForeMap[x][y] = value;
        } else {
            mBackMap[x][y] = value;
        }
    }

    private boolean isSameSlab(Block slab1, Block slab2) {
        if (!(slab1 instanceof Block.Slab) || !(slab2 instanceof Block.Slab)) {
            return false;
        }

        return slab1.getParams().getKey().equals(((Block.Slab) slab2).getOtherPartBlockKey())
                || slab1.getParams().getKey().equals(slab2.getParams().getKey());
    }

    public boolean hasForeAt(int x, int y) {
        return getMap(x, y, FOREGROUND_Z) != mGameItemsHolder.getFallbackBlock();
    }

    public boolean hasBackAt(int x, int y) {
        return getMap(x, y, BACKGROUND_Z) != mGameItemsHolder.getFallbackBlock();
    }

    public Block getForeMap(int x, int y) {
        return getMap(x, y, FOREGROUND_Z);
    }

    public void setForeMap(int x, int y, Block block) {
        setMap(x, y, FOREGROUND_Z, block);
    }

    public void resetForeMap(int x, int y) {
        setForeMap(x, y, mGameItemsHolder.getFallbackBlock());
    }

    public Block getBackMap(int x, int y) {
        return getMap(x, y, BACKGROUND_Z);
    }

    public void setBackMap(int x, int y, Block block) {
        setMap(x, y, BACKGROUND_Z, block);
    }

    public boolean canPlaceToForeground(int x, int y, Block value) {
        return !hasForeAt(x, y) || value == mGameItemsHolder.getFallbackBlock() || !getForeMap(x, y).hasCollision();
    }

    public boolean placeToForeground(int x, int y, Block value) {
        if (canPlaceToForeground(x, y, value)) {
            setForeMap(x, y, value);
            return true;
        } else if (value instanceof Block.Slab && isSameSlab(value, getForeMap(x, y))) {
            setForeMap(x, y, mGameItemsHolder.getBlock(((Block.Slab) value).getFullBlockKey()));
            return true;
        }
        return false;
    }

    public boolean placeToBackground(int x, int y, Block value) {
        if (value == mGameItemsHolder.getFallbackBlock() || (getBackMap(x, y) == mGameItemsHolder.getFallbackBlock() && value.hasCollision()) &&
                (!value.isTransparent() || value == mGameItemsHolder.getBlock("glass") || value.isChest() || value.isSlab())) {
            setBackMap(x, y, value);
            return true;
        }
        return false;
    }

    private void playerDurateTool() {
        final InventoryItem playerCurrentItem = mMobsController.getPlayer().inventory.getActiveItem();
        if (playerCurrentItem.getItem().isTool()) {
            mMobsController.getPlayer().decreaseCurrentItemCount(mGameItemsHolder);
        }
    }

    private boolean shouldDrop(Block block) {
        final Item item = mMobsController.getPlayer().inventory.getActiveItem().getItem();
        int toolLevel = item.isTool() ? ((Item.Tool) item).getLevel() : 0;
        if (item.isTool() && block.getParams().getToolType() != item.getClass()) {
            toolLevel = 0;
        }
        return toolLevel >= block.getParams().getToolLevel();
    }

    public void destroyForeMap(int x, int y) {
        Block block = getForeMap(x, y);
        if (block.isContainer()) {
            mContainerController.destroyContainer(x, y, FOREGROUND_Z);
        }
        if (block.hasDrop() && shouldDrop(block)) {
            for (int i = 0; i < block.getParams().getDropInfo().getCount(); i++) {
                mDropController.addDrop(transformX(x) * 16 + 4, y * 16 + 4, mGameItemsHolder.getItem(block.getDrop()));
            }
        }
        playerDurateTool();
        placeToForeground(x, y, mGameItemsHolder.getFallbackBlock());
    }

    public WorldGeneratorConfig getWorldConfig() {
        return mWorldConfig;
    }

    public void destroyBackMap(int x, int y) {
        Block block = getBackMap(x, y);
        if (block.hasDrop() && shouldDrop(block)) {
            for (int i = 0; i < block.getParams().getDropInfo().getCount(); i++) {
                mDropController.addDrop(transformX(x) * 16 + 4, y * 16 + 4, mGameItemsHolder.getItem(block.getDrop()));
            }
        }
        playerDurateTool();
        placeToBackground(x, y, mGameItemsHolder.getFallbackBlock());
    }

    @CheckForNull
    private Container getContainerAt(int x, int y, int z) {
        return mContainerController.getContainer(transformX(x), y, z);
    }

    @CheckForNull
    public Container getForegroundContainer(int x, int y) {
        return getContainerAt(x, y, FOREGROUND_Z);
    }

    @CheckForNull
    public Container getBackgroundContainer(int x, int y) {
        return getContainerAt(x, y, BACKGROUND_Z);
    }

    @CheckForNull
    public Furnace getForegroundFurnace(int x, int y) {
        @CheckForNull
        final Container container = getForegroundContainer(x, y);

        if (container instanceof Furnace) {
            return (Furnace) container;
        }

        return null;
    }

    @CheckForNull
    public Furnace getBackgroundFurnace(int x, int y) {
        @CheckForNull
        final Container container = getBackgroundContainer(x, y);

        if (container instanceof Furnace) {
            return (Furnace) container;
        }

        return null;
    }
}