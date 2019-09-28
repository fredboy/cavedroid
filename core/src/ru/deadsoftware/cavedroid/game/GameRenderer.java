package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.GameScreen;
import ru.deadsoftware.cavedroid.game.mobs.Mob;
import ru.deadsoftware.cavedroid.game.objects.Drop;
import ru.deadsoftware.cavedroid.misc.ControlMode;
import ru.deadsoftware.cavedroid.misc.Renderer;

import static ru.deadsoftware.cavedroid.GameScreen.GP;
import static ru.deadsoftware.cavedroid.misc.Assets.guiMap;
import static ru.deadsoftware.cavedroid.misc.Assets.textureRegions;

public class GameRenderer extends Renderer {

    GameRenderer(float width, float height) {
        super(width, height);
        Gdx.gl.glClearColor(0f, .6f, .6f, 1f);
    }

    private float drawX(int x) {
        return x * 16 - getCamX();
    }

    private float drawY(int y) {
        return y * 16 - getCamY();
    }

    private void drawWreck(int bl) {
        if (GP.input.getBlockDamage() > 0) {
            int index = 10 * GP.input.getBlockDamage() / GameItems.getBlock(bl).getHp();
            String key = "break_" + index;
            spriter.draw(textureRegions.get(key), GP.input.getCurX() * 16 - getCamX(), GP.input.getCurY() * 16 - getCamY());
        }
    }

    private void drawBlock(int x, int y, boolean drawBG) {
        if (drawBG) {
            if ((!GP.world.hasForeAt(x, y) || GP.world.getForeMapBlock(x, y).isTransparent())
                    && GP.world.hasBackAt(x, y)) {
                spriter.draw(GP.world.getBackMapBlock(x, y).getTex(), drawX(x), drawY(y));
                if (!GP.world.hasForeAt(x, y) && x == GP.input.getCurX() && y == GP.input.getCurY())
                    drawWreck(GP.world.getBackMap(GP.input.getCurX(), GP.input.getCurY()));
            }
        }
        if (GP.world.hasForeAt(x, y) && GP.world.getForeMapBlock(x, y).isBackground() == drawBG) {
            spriter.draw(GP.world.getForeMapBlock(x, y).getTex(), drawX(x), drawY(y));
            if (x == GP.input.getCurX() && y == GP.input.getCurY())
                drawWreck(GP.world.getForeMap(GP.input.getCurX(), GP.input.getCurY()));
        }
    }

    private void drawWorld(boolean bg) {
        int minX = (int) (getCamX() / 16) - 1;
        int minY = (int) (getCamY() / 16) - 1;
        int maxX = (int) ((getCamX() + getWidth()) / 16) + 1;
        int maxY = (int) ((getCamY() + getHeight()) / 16) + 1;
        if (minY < 0) minY = 0;
        if (maxY > GP.world.getHeight()) maxY = GP.world.getHeight();
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
                    if ((!GP.world.hasForeAt(x, y) || GP.world.getForeMapBlock(x, y).isTransparent())
                            && GP.world.hasBackAt(x, y))
                        shaper.rect(drawX(x), drawY(y), 16, 16);
                }
            }
            shaper.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
            spriter.begin();
        }
    }

    private void drawMob(Mob mob) {
        float mobDrawX = mob.pos.x - getCamX();
        float mobDrawY = mob.pos.y - getCamY();

        if (mobDrawX + mob.getWidth() - GP.world.getWidthPx() >= 0 && mobDrawX - GP.world.getWidthPx() <= getWidth())
            mob.draw(spriter, mobDrawX - GP.world.getWidthPx(), mobDrawY);

        if (mobDrawX + mob.getWidth() >= 0 && mobDrawX <= getWidth())
            mob.draw(spriter, mobDrawX, mobDrawY);

        if (mobDrawX + mob.getWidth() + GP.world.getWidthPx() >= 0 && mobDrawX + GP.world.getWidthPx() <= getWidth())
            mob.draw(spriter, mobDrawX + GP.world.getWidthPx(), mobDrawY);
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
                y + 18 + (GP.input.getCreativeScroll() * (72f / GameProc.MAX_CREATIVE_SCROLL)));
        for (int i = GP.input.getCreativeScroll() * 8; i < GP.input.getCreativeScroll() * 8 + 40; i++) {
            if (i > 0 && i < GameItems.getItemsSize())
                if (GameItems.getItem(i).isBlock()) {
                    spriter.draw(GameItems.getBlock(GameItems.getBlockIdByItemId(i)).getTex(),
                            x + 8 + ((i - GP.input.getCreativeScroll() * 8) % 8) * 18,
                            y + 18 + ((i - GP.input.getCreativeScroll() * 8) / 8) * 18);
                } else {
                    spriter.draw(GameItems.getItem(i).getTex(),
                            x + 8 + ((i - GP.input.getCreativeScroll() * 8) % 8) * 18,
                            y + 18 + ((i - GP.input.getCreativeScroll() * 8) / 8) * 18);
                }
        }
        for (int i = 0; i < 9; i++) {
            if (GP.player.inventory[i] > 0)
                if (GameItems.getItem(GP.player.inventory[i]).isBlock()) {
                    spriter.draw(GameItems.getBlock(GameItems.getBlockIdByItemId(GP.player.inventory[i])).getTex(),
                            x + 8 + i * 18, y + creative.getRegionHeight() - 24);
                } else {
                    spriter.draw(GameItems.getItem(GP.player.inventory[i]).getTex(),
                            x + 8 + i * 18, y + creative.getRegionHeight() - 24);
                }
        }

    }

    private void drawGUI() {
        TextureRegion cursor = textureRegions.get("cursor");
        TextureRegion hotbar = textureRegions.get("hotbar");
        TextureRegion hotbarSelector = textureRegions.get("hotbar_selector");

        if (GP.world.hasForeAt(GP.input.getCurX(), GP.input.getCurY()) ||
                GP.world.hasBackAt(GP.input.getCurX(), GP.input.getCurY()) ||
                GP.controlMode == ControlMode.CURSOR ||
                !CaveGame.TOUCH)
            spriter.draw(cursor,
                    GP.input.getCurX() * 16 - getCamX(),
                    GP.input.getCurY() * 16 - getCamY());
        spriter.draw(hotbar, getWidth() / 2 - (float) hotbar.getRegionWidth() / 2, 0);
        for (int i = 0; i < 9; i++) {
            if (GP.player.inventory[i] > 0) {
                if (GameItems.getItem(GP.player.inventory[i]).isBlock()) {
                    spriter.draw(GameItems.getBlock(GameItems.getBlockIdByItemId(GP.player.inventory[i])).getTex(),
                            getWidth() / 2 - (float) hotbar.getRegionWidth() / 2 + 3 + i * 20,
                            3);
                } else {
                    spriter.draw(GameItems.getItem(GP.player.inventory[i]).getTex(),
                            getWidth() / 2 - (float) hotbar.getRegionWidth() / 2 + 3 + i * 20,
                            3);
                }
            }
        }
        spriter.draw(hotbarSelector,
                getWidth() / 2 - (float) hotbar.getRegionWidth() / 2 - 1 + 20 * GP.player.slot,
                -1);
    }

    private void drawTouchGui() {
        for (int i = 0; i < guiMap.size; i++) {
            Rectangle touchKey = guiMap.getValueAt(i);
            spriter.draw(textureRegions.get(guiMap.getKeyAt(i)),
                    touchKey.x, touchKey.y, touchKey.width, touchKey.height);
        }
        if (GP.controlMode == ControlMode.CURSOR) {
            spriter.draw(textureRegions.get("shade"), 83, getHeight() - 21);
        }
    }

    private void drawGamePlay() {
        drawWorld(true);
        GP.player.draw(spriter, GP.player.pos.x - getCamX() - 2, GP.player.pos.y - getCamY());
        for (Mob mob : GP.mobs) drawMob(mob);
        for (Drop drop : GP.drops) drawDrop(drop);
        drawWorld(false);
        drawGUI();
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriter.begin();
        switch (CaveGame.GAME_STATE) {
            case PLAY:
                drawGamePlay();
                break;
            case CREATIVE_INV:
                drawGamePlay();
                drawCreative();
                break;
        }

        if (CaveGame.TOUCH) drawTouchGui();

        spriter.end();

        if (GameScreen.SHOW_MAP) {
            //DRAW MAP
            shaper.begin(ShapeRenderer.ShapeType.Filled);
            shaper.setColor(Color.LIGHT_GRAY);
            shaper.rect(0, 0, GP.world.getWidth(), 128);
            for (int y = 128; y < 256; y++) {
                for (int x = 0; x < getWidth(); x++) {
                    if (GP.world.hasForeAt(x, y) || GP.world.hasBackAt(x, y)) {
                        if (GameItems.isWater(GP.world.getForeMap(x, y))) {
                            shaper.setColor(Color.BLUE);
                        } else if (GameItems.isLava(GP.world.getForeMap(x, y))) {
                            shaper.setColor(Color.RED);
                        } else {
                            if (GP.world.hasForeAt(x, y)) {
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
            shaper.rect(GP.player.pos.x / 16, GP.player.pos.y / 16 - 128, 1, 2);
            shaper.end();
            //=================
        }

        if (GameScreen.SHOW_DEBUG) {
            spriter.begin();
            drawString("FPS: " + GameScreen.FPS, 0, 0);
            drawString("X: " + (int) (GP.player.pos.x / 16), 0, 10);
            drawString("Y: " + (int) (GP.player.pos.y / 16), 0, 20);
            drawString("CurX: " + GP.input.getCurX(), 0, 30);
            drawString("CurY: " + GP.input.getCurY(), 0, 40);
            drawString("Mobs: " + GP.mobs.size(), 0, 50);
            drawString("Drops: " + GP.drops.size(), 0, 60);
            drawString("Block: " + GameItems.getBlockKey(GP.world.getForeMap(GP.input.getCurX(), GP.input.getCurY())), 0, 70);
            drawString("Hand: " + GameItems.getItemKey(GP.player.inventory[GP.player.slot]), 0, 80);
            drawString("Game mode: " + GP.player.gameMode, 0, 90);
            spriter.end();
        }


    }

}
