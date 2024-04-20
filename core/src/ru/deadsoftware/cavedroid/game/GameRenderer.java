package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.mobs.Mob;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.mobs.Player;
import ru.deadsoftware.cavedroid.game.model.block.Block;
import ru.deadsoftware.cavedroid.game.model.item.Item;
import ru.deadsoftware.cavedroid.game.objects.Drop;
import ru.deadsoftware.cavedroid.game.objects.DropController;
import ru.deadsoftware.cavedroid.game.world.GameWorld;
import ru.deadsoftware.cavedroid.misc.ControlMode;
import ru.deadsoftware.cavedroid.misc.Renderer;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static ru.deadsoftware.cavedroid.misc.Assets.*;

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

    private void drawWreck(Block bl) {
        if (mGameInput.getBlockDamage() > 0) {
            int index = 10 * mGameInput.getBlockDamage() / bl.getHp();
            String key = "break_" + index;

            if (index > 10 || index < 0) {
                return;
            }

            spriter.draw(textureRegions.get(key), mGameInput.getCurX() * 16 - getCamX(),
                    mGameInput.getCurY() * 16 - getCamY());
        }
    }

    private void drawBlock(int x, int y, boolean drawBG) {
        if (drawBG) {
            if ((!mGameWorld.hasForeAt(x, y) || mGameWorld.getForeMap(x, y).isTransparent())
                    && mGameWorld.hasBackAt(x, y)) {
                mGameWorld.getBackMap(x, y).draw(spriter, drawX(x), drawY(y));
                if (!mGameWorld.hasForeAt(x, y) && x == mGameInput.getCurX() && y == mGameInput.getCurY()) {
                    drawWreck(mGameWorld.getBackMap(mGameInput.getCurX(), mGameInput.getCurY()));
                }
            }
        }
        if (mGameWorld.hasForeAt(x, y) && mGameWorld.getForeMap(x, y).isBackground() == drawBG) {
            mGameWorld.getForeMap(x, y).draw(spriter, drawX(x), drawY(y));
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
                    if ((!mGameWorld.hasForeAt(x, y) || mGameWorld.getForeMap(x, y).isTransparent())
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

    private Rectangle getShiftedRectRespectfulToViewPort(final Rectangle rect, final float shift) {
        return new Rectangle(rect.x + shift - getCamX(), rect.y - getCamY(), rect.width, rect.height);
    }

    @CheckForNull
    private Rectangle getDrawingRectIfInsideViewport(final Rectangle rectangle) {
        final Rectangle viewportRect = new Rectangle(0, 0, getWidth(), getHeight());

        final Rectangle notShifted = getShiftedRectRespectfulToViewPort(rectangle, 0);
        if (Intersector.overlaps(viewportRect, notShifted)) {
            return notShifted;
        }

        final Rectangle shiftedLeft = getShiftedRectRespectfulToViewPort(rectangle, -mGameWorld.getWidthPx());
        if (Intersector.overlaps(viewportRect, shiftedLeft)) {
            return shiftedLeft;
        }

        final Rectangle shiftedRight = getShiftedRectRespectfulToViewPort(rectangle, mGameWorld.getWidthPx());
        if (Intersector.overlaps(viewportRect, shiftedRight)) {
            return shiftedRight;
        }

        return null;
    }

    private void drawMob(Mob mob, float delta) {
        float mobDrawX = mob.getX() - getCamX();
        float mobDrawY = mob.getY() - getCamY();

        if (mobDrawX + mob.getWidth() < 0 && mobDrawX + mGameWorld.getWidthPx() > 0) {
            mobDrawX += mGameWorld.getWidthPx();
        } else if (mobDrawX > getWidth() && mobDrawX + mob.getWidth() - mGameWorld.getWidthPx() > 0) {
            mobDrawX -= mGameWorld.getWidthPx();
        } else if (mobDrawX + mob.getWidth() < 0 && mobDrawX > getWidth()) {
            return;
        }

        mob.draw(spriter, mobDrawX, mobDrawY, delta);
    }

    private void drawDrop(Drop drop) {
        if (drop.getId() <= 0) {
            return;
        }

        @CheckForNull final Rectangle drawingRect = getDrawingRectIfInsideViewport(drop);

        if (drawingRect == null) {
            return;
        }

        final Item item = GameItems.getItem(drop.getId());
        final Sprite sprite = item.getSprite();

        sprite.setPosition(drawingRect.x, drawingRect.y);
        sprite.setSize(drawingRect.width, drawingRect.height);
        sprite.draw(spriter);
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
                spriter.draw(GameItems.getItem(i).getSprite(),
                        x + 8 + ((i - mGameInput.getCreativeScroll() * 8) % 8) * 18,
                        y + 18 + ((i - mGameInput.getCreativeScroll() * 8) / 8) * 18);
            }
        }
        for (int i = 0; i < 9; i++) {
            if (mMobsController.getPlayer().inventory[i] > 0) {
                spriter.draw(GameItems.getItem(mMobsController.getPlayer().inventory[i]).getSprite(),
                        x + 8 + i * 18, y + creative.getRegionHeight() - 24);
            }
        }

    }

    private void drawHealth(float x, float y) {
        Player player = mMobsController.getPlayer();

        if (player.gameMode == 1) {
            return;
        }

        TextureRegion wholeHeart = textureRegions.get("heart_whole");
        TextureRegion halfHeart = textureRegions.get("heart_half");

        int wholeHearts = player.getHealth() / 2;

        for (int i = 0; i < wholeHearts; i++) {
            spriter.draw(wholeHeart, x + i * wholeHeart.getRegionWidth(), y);
        }

        if (player.getHealth() % 2 == 1) {
            spriter.draw(halfHeart, x + wholeHearts * wholeHeart.getRegionWidth(), y);
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

        float hotbarX = getWidth() / 2 - (float) hotbar.getRegionWidth() / 2;
        spriter.draw(hotbar, hotbarX, 0);
        drawHealth(hotbarX, hotbar.getRegionHeight());

        for (int i = 0; i < 9; i++) {
            if (mMobsController.getPlayer().inventory[i] > 0) {
                spriter.draw(GameItems.getItem(mMobsController.getPlayer().inventory[i]).getSprite(),
                        getWidth() / 2 - (float) hotbar.getRegionWidth() / 2 + 3 + i * 20,
                        3);
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

    private void drawGamePlay(float delta) {
        Player player = mMobsController.getPlayer();

        drawWorld(true);
        player.draw(spriter, player.getX() - getCamX() - player.getWidth() / 2, player.getY() - getCamY(), delta);
        mMobsController.getMobs().forEach((mob) -> {
            drawMob(mob, delta);
        });
        mDropController.forEach(this::drawDrop);
        drawWorld(false);
        drawGUI();
    }

    private void updateCameraPosition() {
        Player player = mMobsController.getPlayer();
        setCamPos(player.getX() + player.getWidth() / 2 - getWidth() / 2,
                player.getY() + player.getHeight() / 2 - getHeight() / 2);
    }

    @Nullable
    private Color getMinimapColor(int x, int y) {
        @Nullable Color result = null;

        final boolean hasForeMap = mGameWorld.hasForeAt(x, y);
        final boolean hasBackMap = mGameWorld.hasBackAt(x, y);

        if (hasForeMap) {
            final Block block = mGameWorld.getForeMap(x, y);

            if (GameItems.isWater(block)) {
                result = Color.BLUE;
            } else if (GameItems.isLava(block)) {
                result = Color.RED;
            } else {
                result = Color.BLACK;
            }
        } else if (hasBackMap) {
            result = Color.DARK_GRAY;
        }

        return result;
    }

    private void drawMiniMap(float miniMapX, float miniMapY, float size) {
        shaper.begin(ShapeRenderer.ShapeType.Filled);

        shaper.setColor(Color.LIGHT_GRAY);
        shaper.rect(miniMapX, miniMapY, size, size);

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {

                final int worldX = (int) (mMobsController.getPlayer().getMapX() - size / 2 + x);
                final int worldY = (int) (mMobsController.getPlayer().getUpperMapY() - size / 2 + y);

                @Nullable final Color color = getMinimapColor(worldX, worldY);

                if (color != null) {
                    shaper.setColor(color);
                    shaper.rect(miniMapX + x, miniMapY + y, 1, 1);
                }
            }
        }

        shaper.setColor(Color.OLIVE);
        shaper.rect(miniMapX + size / 2, miniMapY + size / 2, 1, 2);
        shaper.end();
    }

    @Override
    public void render(float delta) {
        int fps = MathUtils.ceil(1 / delta);
        updateCameraPosition();
        mGameInput.moveCursor(this);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriter.begin();

        drawGamePlay(delta);

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
            drawMiniMap(getWidth() - 64f - 24f, 24f, 64f);
        }

        if (mMainConfig.isShowInfo()) {
            spriter.begin();
            Player player = mMobsController.getPlayer();
            drawString("FPS: " + fps, 0, 0);
            drawString("X: " + player.getMapX(), 0, 10);
            drawString("Y: " + player.getUpperMapY(), 0, 20);
            drawString("CurX: " + mGameInput.getCurX(), 0, 30);
            drawString("CurY: " + mGameInput.getCurY(), 0, 40);
            drawString("Velocity: " + player.getVelocity(), 0, 50);
            drawString("Swim: " + player.swim, 0, 60);
            drawString("Mobs: " + mMobsController.getMobs().size(), 0, 70);
            drawString("Drops: " + mDropController.getSize(), 0, 80);
            drawString("Block: " + mGameWorld.getForeMap(mGameInput.getCurX(), mGameInput.getCurY()).getParams().getKey(), 0, 90);
            drawString("Hand: " + GameItems.getItemKey(mMobsController.getPlayer().inventory[mMobsController.getPlayer().slot]), 0, 100);
            drawString("Game mode: " + player.gameMode, 0, 110);
            spriter.end();
        }

    }

}
