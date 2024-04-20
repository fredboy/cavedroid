package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.google.common.collect.Range;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.actions.CommonBlockActionUtilsKt;
import ru.deadsoftware.cavedroid.game.actions.placeblock.IPlaceBlockAction;
import ru.deadsoftware.cavedroid.game.actions.useitem.IUseItemAction;
import ru.deadsoftware.cavedroid.game.mobs.Mob;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.mobs.Pig;
import ru.deadsoftware.cavedroid.game.mobs.Player;
import ru.deadsoftware.cavedroid.game.model.item.Item;
import ru.deadsoftware.cavedroid.game.objects.DropController;
import ru.deadsoftware.cavedroid.game.world.GameWorld;
import ru.deadsoftware.cavedroid.misc.Assets;
import ru.deadsoftware.cavedroid.misc.ControlMode;

import javax.annotation.CheckForNull;
import javax.inject.Inject;

import java.util.Map;

@GameScope
public class GameInput {

    private static final String TAG = "GameInput";

    private final MainConfig mMainConfig;
    private final GameWorld mGameWorld;
    private final DropController mDropController;
    private final MobsController mMobsController;
    private final GameItemsHolder mGameItemsHolder;
    private final Map<String, IUseItemAction> mUseItemActionMap;
    private final Map<String, IPlaceBlockAction> mPlaceBlockActionMap;

    private final Player mPlayer;

    private ControlMode mControlMode;

    private boolean mKeyDown;
    private boolean mTouchedDown;
    private boolean mDragging;

    private int mKeyDownCode;
    private int mTouchDownBtn;
    private float mTouchDownX;
    private float mTouchDownY;
    private long mTouchDownTime;

    private int mCurX;
    private int mCurY;
    private int mCreativeScroll;
    private int mBlockDamage;

    @Inject
    public GameInput(MainConfig mainConfig,
                     GameWorld gameWorld,
                     DropController dropController,
                     MobsController mobsController,
                     GameItemsHolder gameItemsHolder,
                     Map<String, IUseItemAction> useItemActionMap,
                     Map<String, IPlaceBlockAction> placeBlockActionMap) {
        mMainConfig = mainConfig;
        mGameWorld = gameWorld;
        mDropController = dropController;
        mMobsController = mobsController;
        mGameItemsHolder = gameItemsHolder;
        mUseItemActionMap = useItemActionMap;
        mPlaceBlockActionMap = placeBlockActionMap;

        mPlayer = mMobsController.getPlayer();

        mControlMode = mMainConfig.isTouch() ? ControlMode.WALK : ControlMode.CURSOR;
    }

    private boolean checkSwim() {
        return mGameWorld.getForeMap(mPlayer.getMapX(), mPlayer.getLowerMapY()).isFluid();
    }

    private void goUpwards() {
        if (checkSwim()) {
            mPlayer.swim = true;
        } else if (mPlayer.canJump()) {
            mPlayer.jump();
        } else if (!mPlayer.isFlyMode() && mPlayer.gameMode == 1) {
            mPlayer.setFlyMode(true);
            mPlayer.getVelocity().y = 0;
        } else if (mPlayer.isFlyMode()) {
            mPlayer.getVelocity().y = -mPlayer.getSpeed();
        }
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private boolean insideCreativeInv(float screenX, float screenY) {
        TextureRegion creative = Assets.textureRegions.get("creative");
        return (screenX > mMainConfig.getWidth() / 2 - creative.getRegionWidth() / 2 &&
                screenX < mMainConfig.getWidth() / 2 + creative.getRegionWidth() / 2 &&
                screenY > mMainConfig.getHeight() / 2 - creative.getRegionHeight() / 2 &&
                screenY < mMainConfig.getHeight() / 2 + creative.getRegionHeight() / 2);
    }

    private void wasdPressed(int keycode) {
        if (mControlMode == ControlMode.WALK || !mMainConfig.isTouch()) {
            switch (keycode) {
                case Input.Keys.A:
                    mPlayer.getVelocity().x = -mPlayer.getSpeed();
                    mPlayer.setDir(Mob.Direction.LEFT);
                    if (mMainConfig.isTouch() && checkSwim()) {
                        mPlayer.swim = true;
                    }
                    break;
                case Input.Keys.D:
                    mPlayer.getVelocity().x = mPlayer.getSpeed();
                    mPlayer.setDir(Mob.Direction.RIGHT);
                    if (mMainConfig.isTouch() && checkSwim()) {
                        mPlayer.swim = true;
                    }
                    break;
                case Input.Keys.W:
                case Input.Keys.SPACE:
                    goUpwards();
                    break;
                case Input.Keys.S:
                case Input.Keys.CONTROL_LEFT:
                    mPlayer.getVelocity().y = mPlayer.getSpeed();
                    break;
            }
        } else {
            switch (keycode) {
                case Input.Keys.A:
                    mCurX--;
                    break;
                case Input.Keys.D:
                    mCurX++;
                    break;
                case Input.Keys.W:
                    mCurY--;
                    break;
                case Input.Keys.S:
                    mCurY++;
                    break;
            }
            mBlockDamage = 0;
        }
    }

    private boolean isNotAutoselectable(int x, int y) {
        return (!mGameWorld.hasForeAt(x, y) || !mGameWorld.getForeMap(x, y).hasCollision());
    }

    private void checkCursorBounds() {
        if (mCurY < 0) {
            mCurY = 0;
        } else if (mCurY >= mGameWorld.getHeight()) {
            mCurY = mGameWorld.getHeight() - 1;
        }

        if (mControlMode == ControlMode.CURSOR) {
            if (mCurX * 16 + 8 < mPlayer.getX() + mPlayer.getWidth() / 2) {
                mPlayer.setDir(Mob.Direction.LEFT);
            } else {
                mPlayer.setDir(Mob.Direction.RIGHT);
            }
        }
    }

    public void moveCursor(GameRenderer gameRenderer) {
        int pastX = mCurX;
        int pastY = mCurY;

        if (mControlMode == ControlMode.WALK && mMainConfig.isTouch()) {
            mCurX = mPlayer.getMapX() + (mPlayer.looksLeft() ? -1 : 1);
            mCurY = mPlayer.getUpperMapY();
            for (int i = 0; i < 2 && isNotAutoselectable(mCurX, mCurY); i++) {
                mCurY++;
            }
            if (isNotAutoselectable(mCurX, mCurY)) {
                mCurX += mPlayer.looksLeft() ? 1 : -1;
            }
        } else if (!mMainConfig.isTouch()) {
            final int tmpX = (int) (Gdx.input.getX() * (mMainConfig.getWidth() /
                    Gdx.graphics.getWidth()) + gameRenderer.getCamX());
            mCurX = tmpX / 16;

            final int tmpY = (int) (Gdx.input.getY() * (mMainConfig.getHeight() /
                    Gdx.graphics.getHeight()) + gameRenderer.getCamY());
            mCurY = tmpY / 16;

            if (tmpX < 0) {
                mCurX--;
            }

            final double a = tmpX - mPlayer.x;
            final double b = tmpY - mPlayer.y;

            mPlayer.headRotation = (float) Math.atan(b / a) * MathUtils.radDeg;
        }

        if (pastX != mCurX || pastY != mCurY) {
            mBlockDamage = 0;
        }

        checkCursorBounds();
    }

    private void useItem(int x, int y, @CheckForNull Item item, boolean bg) {
        mPlayer.startHitting();

        if (item == null) {
            return;
        }

        if (item instanceof Item.Placeable) {
            if (!bg) {
                CommonBlockActionUtilsKt.placeToForegroundAction(mPlaceBlockActionMap, (Item.Placeable) item, x, y);
            } else {
                CommonBlockActionUtilsKt.placeToBackgroundAction(mPlaceBlockActionMap, (Item.Placeable) item, x, y);
            }
        } else if (item instanceof Item.Usable) {
            final String actionKey = ((Item.Usable) item).getUseActionKey();
            final IUseItemAction useItemAction = mUseItemActionMap.get(actionKey);

            if (useItemAction != null) {
                useItemAction.perform((Item.Usable) item, x, y);
            } else {
                Gdx.app.error(TAG, "use item action " + actionKey + " not found");
            }
        }
    }

    private void hitMobs() {
        final Player player = mMobsController.getPlayer();
        mMobsController.getMobs().forEach((mob) -> {
            if (Intersector.overlaps(mob, player)) {
                mob.damage(5);
                mob.jump();
            }
        });
    }

    private void pressLMB() {
        if (mMainConfig.checkGameUiWindow(GameUiWindow.NONE)) {
            mPlayer.startHitting();

            if ((mGameWorld.hasForeAt(mCurX, mCurY) && mGameWorld.getForeMap(mCurX, mCurY).getHp() >= 0) ||
                    (!mGameWorld.hasForeAt(mCurX, mCurY) && mGameWorld.hasBackAt(mCurX, mCurY) &&
                            mGameWorld.getBackMap(mCurX, mCurY).getHp() >= 0)) {
                if (mPlayer.gameMode == 0) {
                    mBlockDamage++;
                    if (mGameWorld.hasForeAt(mCurX, mCurY)) {
                        if (mBlockDamage >= mGameWorld.getForeMap(mCurX, mCurY).getHp()) {
                            mGameWorld.destroyForeMap(mCurX, mCurY);
                            mBlockDamage = 0;
                        }
                    } else if (mGameWorld.hasBackAt(mCurX, mCurY)) {
                        if (mBlockDamage >= mGameWorld.getBackMap(mCurX, mCurY).getHp()) {
                            mGameWorld.destroyBackMap(mCurX, mCurY);
                            mBlockDamage = 0;
                        }
                    }
                } else {
                    if (mGameWorld.hasForeAt(mCurX, mCurY)) {
                        mGameWorld.placeToForeground(mCurX, mCurY, mGameItemsHolder.getFallbackBlock());
                    } else if (mGameWorld.hasBackAt(mCurX, mCurY)) {
                        mGameWorld.placeToBackground(mCurX, mCurY, mGameItemsHolder.getFallbackBlock());
                    }
                    mTouchedDown = false;
                }
            } else {
                hitMobs();
                mTouchedDown = false;
            }
        }
    }

    private boolean insideHotbar(float x, float y) {
        TextureRegion hotbar = Assets.textureRegions.get("hotbar");
        return y < hotbar.getRegionHeight() &&
                Range.open(mMainConfig.getWidth() / 2 - (float) hotbar.getRegionWidth() / 2,
                        mMainConfig.getWidth() / 2 + (float) hotbar.getRegionWidth() / 2).contains(x);
    }

    private void holdMB() {
        if (mTouchDownBtn == Input.Buttons.RIGHT) {
            useItem(mCurX, mCurY, mPlayer.inventory[mPlayer.slot].getItem(), true);
            mTouchedDown = false;
        } else {
            if (insideHotbar(mTouchDownX, mTouchDownY)) {
                mMainConfig.setGameUiWindow(GameUiWindow.CREATIVE_INVENTORY);
                mTouchedDown = false;
            }
        }
    }

    public void keyDown(int keycode) {
        mKeyDown = true;
        mKeyDownCode = keycode;
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
                if (mMainConfig.isTouch()) {
                    mControlMode = mControlMode == ControlMode.WALK ? ControlMode.CURSOR : ControlMode.WALK;
                }
                break;

            case Input.Keys.E:
                if (mMainConfig.checkGameUiWindow(GameUiWindow.NONE)) {
                    switch (mPlayer.gameMode) {
                        case 0:
                            //TODO survival inv
                            break;
                        case 1:
                            mMainConfig.setGameUiWindow(GameUiWindow.CREATIVE_INVENTORY);
                            break;
                    }
                } else {
                    mMainConfig.setGameUiWindow(GameUiWindow.NONE);
                }
                break;

            case Input.Keys.G:
                final Mob pig = new Pig(mCurX * 16, mCurY * 16);
                pig.attachToController(mMobsController);
                break;

            case Input.Keys.GRAVE:
                mMobsController.getPlayer().gameMode = (mMobsController.getPlayer().gameMode + 1) % 2;
                break;

            case Input.Keys.ESCAPE:
            case Input.Keys.BACK:
                GameSaver.save(mMainConfig, mDropController, mMobsController, mGameWorld);
                mMainConfig.getCaveGame().quitGame();
                break;

            case Input.Keys.F1:
                mMainConfig.setShowInfo(!mMainConfig.isShowInfo());
                break;

            case Input.Keys.M:
                mMainConfig.setShowMap(!mMainConfig.isShowMap());
                break;
        }
    }

    public void keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.A:
            case Input.Keys.D:
                mPlayer.getVelocity().x = 0;
                if (mMainConfig.isTouch() && mPlayer.swim) {
                    mPlayer.swim = false;
                }
                break;

            case Input.Keys.W:
            case Input.Keys.S:
            case Input.Keys.SPACE:
            case Input.Keys.CONTROL_LEFT:
                if (mPlayer.isFlyMode()) {
                    mPlayer.getVelocity().y = 0;
                }
                if (mPlayer.swim) {
                    mPlayer.swim = false;
                }
                break;
        }
    }

    public void touchDown(float touchX, float touchY, int button) {
        mTouchDownTime = TimeUtils.millis();
        mTouchedDown = true;
        mTouchDownBtn = button;
        mTouchDownX = touchX;
        mTouchDownY = touchY;
    }

    public void touchUp(float screenX, float screenY, int button) {
        if (mDragging) {
            mDragging = false;
            return;
        }

        if (mMainConfig.isTouch() && mKeyDown) {
            keyUp(mKeyDownCode);
            mKeyDown = false;
        }
        TextureRegion hotbar = Assets.textureRegions.get("hotbar");
        TextureRegion creative = Assets.textureRegions.get("creative");
        if (mTouchedDown) {
            if (mMainConfig.checkGameUiWindow(GameUiWindow.CREATIVE_INVENTORY) && insideCreativeInv(screenX, screenY)) {
                int ix = (int) (screenX - (mMainConfig.getWidth() / 2 - creative.getRegionWidth() / 2 + 8)) / 18;
                int iy = (int) (screenY - (mMainConfig.getHeight() / 2 - creative.getRegionHeight() / 2 + 18)) / 18;
                int itemPos = mCreativeScroll * 8 + (ix + iy * 8);
                if (ix >= 8 || ix < 0 || iy < 0 || iy >= 5) {
                    itemPos = -1;
                }

                System.arraycopy(mPlayer.inventory, 0, mPlayer.inventory, 1, 8);
                mPlayer.inventory[0] = mGameItemsHolder.getItemFromCreativeInventory(itemPos).toInventoryItem();
            } else if (mMainConfig.checkGameUiWindow(GameUiWindow.CREATIVE_INVENTORY)) {
                mMainConfig.setGameUiWindow(GameUiWindow.NONE);
            } else if (screenY < hotbar.getRegionHeight() &&
                    screenX > mMainConfig.getWidth() / 2 - (float) hotbar.getRegionWidth() / 2 &&
                    screenX < mMainConfig.getWidth() / 2 + (float) hotbar.getRegionWidth() / 2) {
                mPlayer.slot = (int) ((screenX - (mMainConfig.getWidth() / 2 - hotbar.getRegionWidth() / 2)) / 20);
            } else if (button == Input.Buttons.RIGHT) {
                useItem(mCurX, mCurY,
                        mPlayer.inventory[mPlayer.slot].getItem(), false);
            } else if (button == Input.Buttons.LEFT) {
                mBlockDamage = 0;
            }
        }
        mTouchedDown = false;
    }

    public void touchDragged(float screenX, float screenY) {
        if (Math.abs(screenX - mTouchDownX) < 16 && Math.abs(screenY - mTouchDownY) < 16) {
            return;
        }

        mDragging = true;
        if (mMainConfig.checkGameUiWindow(GameUiWindow.CREATIVE_INVENTORY) && Math.abs(screenY - mTouchDownY) > 16) {
            if (insideCreativeInv(screenX, screenY)) {
                mCreativeScroll -= (screenY - mTouchDownY) / 16;
                mTouchDownX = screenX;
                mTouchDownY = screenY;
                if (mCreativeScroll < 0) {
                    mCreativeScroll = 0;
                }

                final int maxScroll = mGameItemsHolder.getCreativeScrollAmount();
                if (mCreativeScroll > maxScroll) {
                    mCreativeScroll = maxScroll;
                }
            }
        }
    }

    public void scrolled(float amountX, float amountY) {
        switch (mMainConfig.getGameUiWindow()) {
            case NONE:
                mPlayer.slot += (int) amountY;
                if (mPlayer.slot < 0) {
                    mPlayer.slot = 8;
                }
                if (mPlayer.slot > 8) {
                    mPlayer.slot = 0;
                }
                break;
            case CREATIVE_INVENTORY:
                mCreativeScroll += (int) amountY;
                if (mCreativeScroll < 0) {
                    mCreativeScroll = 0;
                }

                final int maxScroll = mGameItemsHolder.getCreativeScrollAmount();
                if (mCreativeScroll > maxScroll) {
                    mCreativeScroll = maxScroll;
                }
                break;
        }
    }

    public int getKeyDownCode() {
        return mKeyDownCode;
    }

    public boolean isKeyDown() {
        return mKeyDown;
    }

    int getBlockDamage() {
        return mBlockDamage;
    }

    int getCurX() {
        return mCurX;
    }

    int getCurY() {
        return mCurY;
    }

    int getCreativeScroll() {
        return mCreativeScroll;
    }

    public ControlMode getControlMode() {
        return mControlMode;
    }

    public void setControlMode(ControlMode controlMode) {
        mControlMode = controlMode;
    }

    void update() {
        if (!mTouchedDown) {
            mPlayer.stopHitting();
            return;
        }

        if (mTouchDownBtn == Input.Buttons.LEFT) {
            pressLMB();
        }

        if (mTouchedDown && TimeUtils.timeSinceMillis(mTouchDownTime) > 500) {
            holdMB();
        }
    }

}
