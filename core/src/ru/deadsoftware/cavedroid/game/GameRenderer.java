package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.GameScreen;
import ru.deadsoftware.cavedroid.game.mobs.Mob;
import ru.deadsoftware.cavedroid.game.objects.Drop;
import ru.deadsoftware.cavedroid.misc.Assets;
import ru.deadsoftware.cavedroid.misc.Renderer;

import static ru.deadsoftware.cavedroid.GameScreen.GP;

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
        if (GP.blockDmg > 0) {
            spriter.draw(Assets.wreck[
                            10 * GP.blockDmg /
                                    GameItems.getBlock(bl).getHp()],
                    GP.curX * 16 - getCamX(),
                    GP.curY * 16 - getCamY());
        }
    }

    private void drawBlock(int x, int y, boolean drawBG) {
        if (drawBG) {
            if ((GP.world.getForeMap(x, y) == 0 || GameItems.getBlock(GP.world.getForeMap(x, y)).isTransparent())
                    && GP.world.getBackMap(x, y) > 0) {
                spriter.draw(
                        Assets.blockTex[GameItems.getBlock(GP.world.getBackMap(x, y)).getTex()],
                        drawX(x), drawY(y));
                if (GP.world.getForeMap(x, y) == 0 && x == GP.curX && y == GP.curY)
                    drawWreck(GP.world.getBackMap(GP.curX, GP.curY));
            }
        }
        if (GP.world.getForeMap(x, y) > 0 && GameItems.getBlock(GP.world.getForeMap(x, y)).isBackground() == drawBG) {
            spriter.draw(
                    Assets.blockTex[GameItems.getBlock(GP.world.getForeMap(x, y)).getTex()],
                    drawX(x), drawY(y));
            if (x == GP.curX && y == GP.curY)
                drawWreck(GP.world.getForeMap(GP.curX, GP.curY));
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
                    if ((GP.world.getForeMap(x, y) == 0 || GameItems.getBlock(GP.world.getForeMap(x, y)).isTransparent())
                            && GP.world.getBackMap(x, y) > 0) shaper.rect(drawX(x), drawY(y), 16, 16);
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
        switch (GameItems.getItem(drop.getId()).getType()) {
            case 0:
                Assets.blockTex[GameItems.getItem(drop.getId()).getTex()].setPosition(
                        drop.pos.x - getCamX() - GP.world.getWidthPx(),
                        drop.pos.y - getCamY());
                Assets.blockTex[GameItems.getItem(drop.getId()).getTex()].draw(spriter);
                Assets.blockTex[GameItems.getItem(drop.getId()).getTex()].setPosition(
                        drop.pos.x - getCamX(),
                        drop.pos.y - getCamY());
                Assets.blockTex[GameItems.getItem(drop.getId()).getTex()].draw(spriter);
                Assets.blockTex[GameItems.getItem(drop.getId()).getTex()].setPosition(
                        drop.pos.x - getCamX() + GP.world.getWidthPx(),
                        drop.pos.y - getCamY());
                Assets.blockTex[GameItems.getItem(drop.getId()).getTex()].draw(spriter);
        }
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private void drawCreative() {
        float x = getWidth() / 2 - (float) Assets.creativeInv.getRegionWidth() / 2;
        float y = getHeight() / 2 - (float) Assets.creativeInv.getRegionHeight() / 2;
        spriter.draw(Assets.creativeInv, x, y);
        spriter.draw(Assets.creativeScr, x + 156,
                y + 18 + (GP.creativeScroll * (72f / GP.maxCreativeScroll)));
        for (int i = GP.creativeScroll * 8; i < GP.creativeScroll * 8 + 40; i++) {
            if (i > 0 && i < GameItems.getItemsSize())
                switch (GameItems.getItem(i).getType()) {
                    case 0:
                        spriter.draw(Assets.blockTex[GameItems.getItem(i).getTex()],
                                x + 8 + ((i - GP.creativeScroll * 8) % 8) * 18,
                                y + 18 + ((i - GP.creativeScroll * 8) / 8) * 18);
                        break;
                    case 1:
                    case 2:
                        spriter.draw(Assets.itemTex[GameItems.getItem(i).getTex()],
                                x + 8 + ((i - GP.creativeScroll * 8) % 8) * 18,
                                y + 18 + ((i - GP.creativeScroll * 8) / 8) * 18);
                        break;
                }
        }
        for (int i = 0; i < 9; i++) {
            if (GP.player.inv[i] > 0)
                switch (GameItems.getItem(GP.player.inv[i]).getType()) {
                    case 0:
                        spriter.draw(Assets.blockTex[GameItems.getItem(GP.player.inv[i]).getTex()],
                                x + 8 + i * 18, y + Assets.creativeInv.getRegionHeight() - 24);
                        break;
                    case 1:
                    case 2:
                        spriter.draw(Assets.itemTex[GameItems.getItem(GP.player.inv[i]).getTex()],
                                x + 8 + i * 18, y + Assets.creativeInv.getRegionHeight() - 24);
                        break;
                }
        }
    }

    private void drawGUI() {
        if (GP.world.getForeMap(GP.curX, GP.curY) > 0 ||
                GP.world.getBackMap(GP.curX, GP.curY) > 0 ||
                GP.ctrlMode == 1 ||
                !CaveGame.TOUCH)
            spriter.draw(Assets.guiCur,
                    GP.curX * 16 - getCamX(),
                    GP.curY * 16 - getCamY());
        spriter.draw(Assets.invBar, getWidth() / 2 - (float) Assets.invBar.getRegionWidth() / 2, 0);
        for (int i = 0; i < 9; i++) {
            if (GP.player.inv[i] > 0) {
                switch (GameItems.getItem(GP.player.inv[i]).getType()) {
                    case 0:
                        spriter.draw(Assets.blockTex[GameItems.getItem(GP.player.inv[i]).getTex()],
                                getWidth() / 2 - (float) Assets.invBar.getRegionWidth() / 2 + 3 + i * 20,
                                3);
                        break;
                    case 1:
                    case 2:
                        spriter.draw(Assets.itemTex[GameItems.getItem(GP.player.inv[i]).getTex()],
                                getWidth() / 2 - (float) Assets.invBar.getRegionWidth() / 2 + 3 + i * 20,
                                3);
                        break;
                }
            }
        }
        spriter.draw(Assets.invBarCur,
                getWidth() / 2 - (float) Assets.invBar.getRegionWidth() / 2 - 1 + 20 * GP.player.invSlot,
                -1);
    }

    private void drawTouchGui() {
        spriter.draw(Assets.touchArrows[0], 26, getHeight() - 52);
        spriter.draw(Assets.touchArrows[1], 0, getHeight() - 26);
        spriter.draw(Assets.touchArrows[2], 26, getHeight() - 26);
        spriter.draw(Assets.touchArrows[3], 52, getHeight() - 26);
        spriter.draw(Assets.touchLMB, getWidth() - 52, getHeight() - 26);
        spriter.draw(Assets.touchRMB, getWidth() - 26, getHeight() - 26);
        spriter.draw(Assets.touchMode, 78, getHeight() - 26);
        if (GP.ctrlMode == 1) {
            Assets.shade.setPosition(83, getHeight() - 21);
            Assets.shade.draw(spriter);
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
        switch (CaveGame.STATE) {
            case GAME_PLAY:
                drawGamePlay();
                break;
            case GAME_CREATIVE_INV:
                drawGamePlay();
                drawCreative();
                break;
        }

        if (CaveGame.TOUCH) drawTouchGui();

        if (GameScreen.SHOW_DEBUG) {
            drawString("FPS: " + GameScreen.FPS, 0, 0);
            drawString("X: " + (int) (GP.player.pos.x / 16), 0, 10);
            drawString("Y: " + (int) (GP.player.pos.y / 16), 0, 20);
            drawString("CurX: " + GP.curX, 0, 30);
            drawString("CurY: " + GP.curY, 0, 40);
            drawString("Mobs: " + GP.mobs.size(), 0, 50);
            drawString("Drops: " + GP.drops.size(), 0, 60);
            drawString("Block: " + GameItems.getBlockKey(GP.world.getForeMap(GP.curX, GP.curY)), 0, 70);
            drawString("Game mode: " + GP.player.gameMode, 0, 80);
        }
        spriter.end();
    }

}
