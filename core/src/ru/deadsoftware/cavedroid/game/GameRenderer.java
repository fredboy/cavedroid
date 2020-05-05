package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.mobs.Mob;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.mobs.Player;
import ru.deadsoftware.cavedroid.game.objects.Drop;
import ru.deadsoftware.cavedroid.game.objects.DropController;
import ru.deadsoftware.cavedroid.misc.ControlMode;
import ru.deadsoftware.cavedroid.misc.Renderer;

import javax.inject.Inject;

import static ru.deadsoftware.cavedroid.misc.Assets.guiMap;
import static ru.deadsoftware.cavedroid.misc.Assets.textureRegions;

@GameScope
public class GameRenderer extends Renderer {

    private static final String TAG = "GameRenderer";

    private final MainConfig mMainConfig;
    private final GameInput mGameInput;
    private final GameWorld mGameWorld;
    private final MobsController mMobsController;
    private final DropController mDropController;

    @Inject
    GameRenderer(MainConfig mainConfig,
                 GameInput gameInput,
                 GameWorld gameWorld,
                 MobsController mobsController,
                 DropController dropController) {
        super(mainConfig.getWidth(), mainConfig.getHeight());

        mMainConfig = mainConfig;
        mGameInput = gameInput;
        mGameWorld = gameWorld;
        mMobsController = mobsController;
        mDropController = dropController;

        Gdx.gl.glClearColor(0f, .6f, .6f, 1f);
    }

    private float drawX(int x) {
        return x * 16 - getCamX();
    }

    private float drawY(int y) {
        return y * 16 - getCamY();
    }

    private void drawWreck(int bl) {
        if (mGameInput.getBlockDamage() > 0) {
            int index = 10 * mGameInput.getBlockDamage() / GameItems.getBlock(bl).getHp();
            String key = "break_" + index;
            spriter.draw(textureRegions.get(key), mGameInput.getCurX() * 16 - getCamX(),
                    mGameInput.getCurY() * 16 - getCamY());
        }
    }

    private void drawBlock(int x, int y, boolean drawBG) {
        if (drawBG) {
            if ((!mGameWorld.hasForeAt(x, y) || mGameWorld.getForeMapBlock(x, y).isTransparent())
                    && mGameWorld.hasBackAt(x, y)) {
                spriter.draw(mGameWorld.getBackMapBlock(x, y).getTexture(), drawX(x), drawY(y));
                if (!mGameWorld.hasForeAt(x, y) && x == mGameInput.getCurX() && y == mGameInput.getCurY()) {
                    drawWreck(mGameWorld.getBackMap(mGameInput.getCurX(), mGameInput.getCurY()));
                }
            }
        }
        if (mGameWorld.hasForeAt(x, y) && mGameWorld.getForeMapBlock(x, y).isBackground() == drawBG) {
            spriter.draw(mGameWorld.getForeMapBlock(x, y).getTexture(), drawX(x), drawY(y));
            if (x == mGameInput.getCurX() && y == mGameInput.getCurY()) {
                drawWreck(mGameWorld.getForeMap(mGameInput.getCurX(), mGameInput.getCurY()));
            }
        }
    }

    private void drawWorld(boolean bg) {
        int minX = (int) (getCamX() / 16) - 1;
        int minY = (int) (getCamY() / 16) - 1;
        int maxX = (int) ((getCamX() + getWidth()) / 16) + 1;
        int maxY = (int) ((getCamY() + getHeight()) / 16) + 1;
        if (minY < 0) {
            minY = 0;
        }
        if (maxY > mGameWorld.getHeight()) {
            maxY = mGameWorld.getHeight();
        }
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                drawBlock(x, y, bg);
            }
        }
        if (bg) {
            spriter.end();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shaper.begin(ShapeRenderer.ShapeType.Filled);
            shaper.setColor(0f, 0f, 0f, .5f);
            for (int y = minY; y < maxY; y++) {
                for (int x = minX; x < maxX; x++) {
                    if ((!mGameWorld.hasForeAt(x, y) || mGameWorld.getForeMapBlock(x, y).isTransparent())
                            && mGameWorld.hasBackAt(x, y)) {
                        shaper.rect(drawX(x), drawY(y), 16, 16);
                    }
                }
            }
            shaper.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
            spriter.begin();
        }
    }

    private void drawMob(Mob mob) {
        float mobDrawX = mob.getX() - getCamX();
        float mobDrawY = mob.getY() - getCamY();

        if (mobDrawX + mob.getWidth() < 0 && mobDrawX + mGameWorld.getWidthPx() > 0) {
            mobDrawX += mGameWorld.getWidthPx();
        } else if (mobDrawX > getWidth() && mobDrawX + mob.getWidth() - mGameWorld.getWidthPx() > 0) {
            mobDrawX -= mGameWorld.getWidthPx();
        } else if (mobDrawX + mob.getWidth() < 0 && mobDrawX > getWidth()) {
            return;
        }

        mob.draw(spriter, mobDrawX, mobDrawY);
    }

    private void drawDrop(Drop drop) {
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private void drawCreative() {
        TextureRegion creative = textureRegions.get("creative");
        float x = getWidth() / 2 - (float) creative.getRegionWidth() / 2;
        float y = getHeight() / 2 - (float) creative.getRegionHeight() / 2;
        spriter.draw(creative, x, y);
        spriter.draw(textureRegions.get("handle"), x + 156,
                y + 18 + (mGameInput.getCreativeScroll() * (72f / GameProc.MAX_CREATIVE_SCROLL)));
        for (int i = mGameInput.getCreativeScroll() * 8; i < mGameInput.getCreativeScroll() * 8 + 40; i++) {
            if (i > 0 && i < GameItems.getItemsSize()) {
                if (GameItems.getItem(i).isBlock()) {
                    spriter.draw(GameItems.getBlock(GameItems.getBlockIdByItemId(i)).getTexture(),
                            x + 8 + ((i - mGameInput.getCreativeScroll() * 8) % 8) * 18,
                            y + 18 + ((i - mGameInput.getCreativeScroll() * 8) / 8) * 18);
                } else {
                    spriter.draw(GameItems.getItem(i).getTexture(),
                            x + 8 + ((i - mGameInput.getCreativeScroll() * 8) % 8) * 18,
                            y + 18 + ((i - mGameInput.getCreativeScroll() * 8) / 8) * 18);
                }
            }
        }
        for (int i = 0; i < 9; i++) {
            if (mMobsController.getPlayer().inventory[i] > 0) {
                if (GameItems.getItem(mMobsController.getPlayer().inventory[i]).isBlock()) {
                    spriter.draw(GameItems.getBlock(GameItems.getBlockIdByItemId(mMobsController.getPlayer().inventory[i])).getTexture(),
                            x + 8 + i * 18, y + creative.getRegionHeight() - 24);
                } else {
                    spriter.draw(GameItems.getItem(mMobsController.getPlayer().inventory[i]).getTexture(),
                            x + 8 + i * 18, y + creative.getRegionHeight() - 24);
                }
            }
        }

    }

    private void drawGUI() {
        TextureRegion cursor = textureRegions.get("cursor");
        TextureRegion hotbar = textureRegions.get("hotbar");
        TextureRegion hotbarSelector = textureRegions.get("hotbar_selector");

        if (mGameWorld.hasForeAt(mGameInput.getCurX(), mGameInput.getCurY()) ||
                mGameWorld.hasBackAt(mGameInput.getCurX(), mGameInput.getCurY()) ||
                mGameInput.getControlMode() == ControlMode.CURSOR || mMainConfig.isTouch()) {
            spriter.draw(cursor, mGameInput.getCurX() * 16 - getCamX(), mGameInput.getCurY() * 16 - getCamY());
        }
        spriter.draw(hotbar, getWidth() / 2 - (float) hotbar.getRegionWidth() / 2, 0);
        for (int i = 0; i < 9; i++) {
            if (mMobsController.getPlayer().inventory[i] > 0) {
                if (GameItems.getItem(mMobsController.getPlayer().inventory[i]).isBlock()) {
                    spriter.draw(GameItems.getBlock(GameItems.getBlockIdByItemId(mMobsController.getPlayer().inventory[i])).getTexture(),
                            getWidth() / 2 - (float) hotbar.getRegionWidth() / 2 + 3 + i * 20,
                            3);
                } else {
                    spriter.draw(GameItems.getItem(mMobsController.getPlayer().inventory[i]).getTexture(),
                            getWidth() / 2 - (float) hotbar.getRegionWidth() / 2 + 3 + i * 20,
                            3);
                }
            }
        }
        spriter.draw(hotbarSelector,
                getWidth() / 2 - (float) hotbar.getRegionWidth() / 2 - 1 + 20 * mMobsController.getPlayer().slot,
                -1);
    }

    private void drawTouchGui() {
        for (int i = 0; i < guiMap.size; i++) {
            Rectangle touchKey = guiMap.getValueAt(i).getRect();
            spriter.draw(textureRegions.get(guiMap.getKeyAt(i)),
                    touchKey.x, touchKey.y, touchKey.width, touchKey.height);
        }
        if (mGameInput.getControlMode() == ControlMode.CURSOR) {
            spriter.draw(textureRegions.get("shade"), 83, getHeight() - 21);
        }
    }

    private void drawGamePlay() {
        Player player = mMobsController.getPlayer();

        drawWorld(true);
        player.draw(spriter, player.getX() - getCamX() - player.getWidth() / 2, player.getY() - getCamY());
        mMobsController.forEach(this::drawMob);
        mDropController.forEach(this::drawDrop);
        drawWorld(false);
        drawGUI();
    }

    private void updateCameraPosition() {
        Player player = mMobsController.getPlayer();
        setCamPos(player.getX() + player.getWidth() / 2 - getWidth() / 2,
                player.getY() + player.getHeight() / 2 - getHeight() / 2);
    }

    @Override
    public void render(float delta) {
        int fps = (int) (1 / delta);
        updateCameraPosition();
        mGameInput.moveCursor(this);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriter.begin();

        drawGamePlay();

        switch (mMainConfig.getGameUiWindow()) {
            case CREATIVE_INVENTORY:
                drawCreative();
                break;
            //TODO draw other ui windows
        }


        if (mMainConfig.isTouch()) {
            drawTouchGui();
        }

        spriter.end();

        if (mMainConfig.isShowMap()) {
            //DRAW MAP
            shaper.begin(ShapeRenderer.ShapeType.Filled);
            shaper.setColor(Color.LIGHT_GRAY);
            shaper.rect(0, 0, mGameWorld.getWidth(), 128);
            for (int y = 128; y < 256; y++) {
                for (int x = 0; x < getWidth(); x++) {
                    if (mGameWorld.hasForeAt(x, y) || mGameWorld.hasBackAt(x, y)) {
                        if (GameItems.isWater(mGameWorld.getForeMap(x, y))) {
                            shaper.setColor(Color.BLUE);
                        } else if (GameItems.isLava(mGameWorld.getForeMap(x, y))) {
                            shaper.setColor(Color.RED);
                        } else {
                            if (mGameWorld.hasForeAt(x, y)) {
                                shaper.setColor(Color.BLACK);
                            } else {
                                shaper.setColor(Color.DARK_GRAY);
                            }
                        }
                        shaper.rect(x, y - 128, 1, 1);
                    }
                }
            }
            shaper.setColor(Color.OLIVE);
            shaper.rect(mMobsController.getPlayer().getMapX(), mMobsController.getPlayer().getUpperMapY() - 128, 1, 2);
            shaper.end();
            //=================
        }

        if (mMainConfig.isShowInfo()) {
            spriter.begin();
            drawString("FPS: " + fps, 0, 0);
            drawString("X: " + mMobsController.getPlayer().getMapX(), 0, 10);
            drawString("Y: " + mMobsController.getPlayer().getUpperMapY(), 0, 20);
            drawString("CurX: " + mGameInput.getCurX(), 0, 30);
            drawString("CurY: " + mGameInput.getCurY(), 0, 40);
            drawString("Mobs: " + mMobsController.getSize(), 0, 50);
            drawString("Drops: " + mDropController.getSize(), 0, 60);
            drawString("Block: " + GameItems.getBlockKey(mGameWorld.getForeMap(mGameInput.getCurX(), mGameInput.getCurY())), 0, 70);
            drawString("Hand: " + GameItems.getItemKey(mMobsController.getPlayer().inventory[mMobsController.getPlayer().slot]), 0, 80);
            drawString("Game mode: " + mMobsController.getPlayer().gameMode, 0, 90);
            spriter.end();
        }

    }

}
