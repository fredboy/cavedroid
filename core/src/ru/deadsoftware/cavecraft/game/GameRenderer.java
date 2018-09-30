package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import ru.deadsoftware.cavecraft.CaveGame;
import ru.deadsoftware.cavecraft.GameScreen;
import ru.deadsoftware.cavecraft.game.mobs.Mob;
import ru.deadsoftware.cavecraft.game.objects.Drop;
import ru.deadsoftware.cavecraft.game.objects.Player;
import ru.deadsoftware.cavecraft.misc.Assets;
import ru.deadsoftware.cavecraft.misc.Renderer;

public class GameRenderer extends Renderer {

    private GameProc gp;

    public GameRenderer(GameProc gp, float width, float heigth) {
        super(width, heigth);
        Gdx.gl.glClearColor(0f, .6f, .6f, 1f);
        this.gp = gp;
    }

    private float drawX(int x) {
        return x * 16 - getCamX();
    }

    private float drawY(int y) {
        return y * 16 - getCamY();
    }

    private void drawWreck() {
        if (gp.blockDmg > 0) {
            spriter.draw(Assets.wreck[
                            10 * gp.blockDmg /
                                    Items.blocks.getValueAt(gp.world.getForeMap(gp.curX, gp.curY)).getHp()],
                    gp.curX * 16 - getCamX(),
                    gp.curY * 16 - getCamY());
        }
    }

    private void drawWorldBackground() {
        int minX = (int) (getCamX() / 16) - 1;
        int minY = (int) (getCamY() / 16) - 1;
        int maxX = (int) ((getCamX() + getWidth()) / 16) + 1;
        int maxY = (int) ((getCamY() + getHeight()) / 16) + 1;
        if (minY < 0) minY = 0;
        if (maxY > gp.world.getHeight()) maxY = gp.world.getHeight();
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                if ((gp.world.getForeMap(x, y) == 0 || Items.blocks.getValueAt(gp.world.getForeMap(x, y)).tp)
                        && gp.world.getBackMap(x, y) > 0) {
                    spriter.draw(
                            Assets.blockTex[Items.blocks.getValueAt(gp.world.getBackMap(x, y)).getTex()],
                            drawX(x), drawY(y));
                    if (gp.world.getForeMap(x, y) == 0) drawWreck();
                    Assets.shade.setPosition(drawX(x), drawY(y));
                    Assets.shade.draw(spriter);
                }
                if (gp.world.getForeMap(x, y) > 0 && Items.blocks.getValueAt(gp.world.getForeMap(x, y)).bg) {
                    spriter.draw(
                            Assets.blockTex[Items.blocks.getValueAt(gp.world.getForeMap(x, y)).getTex()],
                            drawX(x), drawY(y));
                    drawWreck();
                }
            }
        }
    }

    private void drawWorldForeground() {
        int minX = (int) (getCamX() / 16) - 1;
        int minY = (int) (getCamY() / 16) - 1;
        int maxX = (int) ((getCamX() + getWidth()) / 16) + 1;
        int maxY = (int) ((getCamY() + getHeight()) / 16) + 1;
        if (minY < 0) minY = 0;
        if (maxY > gp.world.getHeight()) maxY = gp.world.getHeight();
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                if (gp.world.getForeMap(x, y) > 0 && !Items.blocks.getValueAt(gp.world.getForeMap(x, y)).bg) {
                    spriter.draw(
                            Assets.blockTex[Items.blocks.getValueAt(gp.world.getForeMap(x, y)).getTex()],
                            drawX(x), drawY(y));
                    drawWreck();
                }
            }
        }
    }

    private void drawMob(Mob mob) {
        mob.draw(spriter,
                mob.position.x - getCamX() - gp.world.getWidth() * 16, mob.position.y - getCamY());
        mob.draw(spriter,
                mob.position.x - getCamX(), mob.position.y - getCamY());
        mob.draw(spriter,
                mob.position.x - getCamX() + gp.world.getWidth() * 16, mob.position.y - getCamY());
    }

    private void drawDrop(Drop drop) {
        switch (Items.items.get(drop.getId()).getType()) {
            case 0:
                Assets.blockTex[Items.items.get(drop.getId()).getTex()].setPosition(drop.position.x - getCamX() - gp.world.getWidth() * 16, drop.position.y - getCamY());
                Assets.blockTex[Items.items.get(drop.getId()).getTex()].draw(spriter);
                Assets.blockTex[Items.items.get(drop.getId()).getTex()].setPosition(drop.position.x - getCamX(), drop.position.y - getCamY());
                Assets.blockTex[Items.items.get(drop.getId()).getTex()].draw(spriter);
                Assets.blockTex[Items.items.get(drop.getId()).getTex()].setPosition(drop.position.x - getCamX() + gp.world.getWidth() * 16, drop.position.y - getCamY());
                Assets.blockTex[Items.items.get(drop.getId()).getTex()].draw(spriter);
        }
    }

    private void drawPlayer(Player pl) {
        if (pl.move.x != 0 || Assets.plSprite[0][2].getRotation() != 0) {
            Assets.plSprite[0][2].rotate(Player.ANIM_SPEED);
            Assets.plSprite[1][2].rotate(-Player.ANIM_SPEED);
            Assets.plSprite[0][3].rotate(-Player.ANIM_SPEED);
            Assets.plSprite[1][3].rotate(Player.ANIM_SPEED);
        } else {
            Assets.plSprite[0][2].setRotation(0);
            Assets.plSprite[1][2].setRotation(0);
            Assets.plSprite[0][3].setRotation(0);
            Assets.plSprite[1][3].setRotation(0);
        }
        if (Assets.plSprite[0][2].getRotation() >= 60 || Assets.plSprite[0][2].getRotation() <= -60)
            Player.ANIM_SPEED = -Player.ANIM_SPEED;

        //back hand
        Assets.plSprite[1][2].setPosition(
                pl.position.x - getCamX() - 6,
                pl.position.y - getCamY());
        Assets.plSprite[1][2].draw(spriter);
        //back leg
        Assets.plSprite[1][3].setPosition(
                pl.position.x - getCamX() - 6,
                pl.position.y - getCamY() + 10);
        Assets.plSprite[1][3].draw(spriter);
        //front leg
        Assets.plSprite[0][3].setPosition(
                pl.position.x - getCamX() - 6,
                pl.position.y - getCamY() + 10);
        Assets.plSprite[0][3].draw(spriter);
        //head
        spriter.draw(Assets.plSprite[pl.dir][0],
                pl.position.x - getCamX() - 2,
                pl.position.y - getCamY() - 2);
        //body
        spriter.draw(Assets.plSprite[pl.dir][1],
                pl.position.x - getCamX() - 2, pl.position.y - getCamY() + 8);
        //item in hand
        if (pl.inventory[gp.invSlot] > 0)
            switch (Items.items.get(pl.inventory[gp.invSlot]).getType()) {
                case 0:
                    Assets.blockTex[Items.items.get(pl.inventory[gp.invSlot]).getTex()].setPosition(
                            pl.position.x - getCamX() - 8 * MathUtils.sin(MathUtils.degRad * Assets.plSprite[0][2].getRotation()),
                            pl.position.y - getCamY() + 6 + 8 * MathUtils.cos(MathUtils.degRad * Assets.plSprite[0][2].getRotation()));
                    Assets.blockTex[Items.items.get(pl.inventory[gp.invSlot]).getTex()].draw(spriter);
                    break;
                default:
                    Assets.itemTex[Items.items.get(pl.inventory[gp.invSlot]).getTex()].flip((pl.dir == 0), false);
                    Assets.itemTex[Items.items.get(pl.inventory[gp.invSlot]).getTex()].setRotation(
                            -45 + pl.dir * 90 + Assets.plSprite[0][2].getRotation());
                    Assets.itemTex[Items.items.get(pl.inventory[gp.invSlot]).getTex()].setPosition(
                            pl.position.x - getCamX() - 10 + (12 * pl.dir) - 8 * MathUtils.sin(MathUtils.degRad * Assets.plSprite[0][2].getRotation()),
                            pl.position.y - getCamY() + 2 + 8 * MathUtils.cos(MathUtils.degRad * Assets.plSprite[0][2].getRotation()));
                    Assets.itemTex[Items.items.get(pl.inventory[gp.invSlot]).getTex()].draw(spriter);
                    Assets.itemTex[Items.items.get(pl.inventory[gp.invSlot]).getTex()].flip((pl.dir == 0), false);
                    break;
            }
        //front hand
        Assets.plSprite[0][2].setPosition(
                pl.position.x - getCamX() - 6,
                pl.position.y - getCamY());
        Assets.plSprite[0][2].draw(spriter);
    }

    private void drawCreative() {
        float x = getWidth() / 2 - Assets.creativeInv.getRegionWidth() / 2;
        float y = getHeight() / 2 - Assets.creativeInv.getRegionHeight() / 2;
        spriter.draw(Assets.creativeInv, x, y);
        spriter.draw(Assets.creativeScr, x + 156,
                y + 18 + (gp.creativeScroll * (72 / gp.maxCreativeScroll)));
        for (int i = gp.creativeScroll * 8; i < gp.creativeScroll * 8 + 40; i++) {
            if (i > 0 && i < Items.items.size())
                switch (Items.items.get(i).getType()) {
                    case 0:
                        spriter.draw(Assets.blockTex[Items.items.get(i).getTex()],
                                x + 8 + ((i - gp.creativeScroll * 8) % 8) * 18,
                                y + 18 + ((i - gp.creativeScroll * 8) / 8) * 18);
                        break;
                    case 1:
                        spriter.draw(Assets.itemTex[Items.items.get(i).getTex()],
                                x + 8 + ((i - gp.creativeScroll * 8) % 8) * 18,
                                y + 18 + ((i - gp.creativeScroll * 8) / 8) * 18);
                        break;
                }
        }
        for (int i = 0; i < 9; i++) {
            if (gp.player.inventory[i] > 0)
                switch (Items.items.get(gp.player.inventory[i]).getType()) {
                    case 0:
                        spriter.draw(Assets.blockTex[Items.items.get(gp.player.inventory[i]).getTex()],
                                x + 8 + i * 18, y + Assets.creativeInv.getRegionHeight() - 24);
                        break;
                    case 1:
                        spriter.draw(Assets.itemTex[Items.items.get(gp.player.inventory[i]).getTex()],
                                x + 8 + i * 18, y + Assets.creativeInv.getRegionHeight() - 24);
                        break;
                }
        }
    }

    private void drawGUI() {
        if (gp.world.getForeMap(gp.curX, gp.curY) > 0 ||
                gp.world.getBackMap(gp.curX, gp.curY) > 0 ||
                gp.ctrlMode == 1 ||
                !CaveGame.TOUCH)
            spriter.draw(Assets.guiCur,
                    gp.curX * 16 - getCamX(),
                    gp.curY * 16 - getCamY());
        spriter.draw(Assets.invBar, getWidth() / 2 - Assets.invBar.getRegionWidth() / 2, 0);
        for (int i = 0; i < 9; i++) {
            if (gp.player.inventory[i] > 0) {
                switch (Items.items.get(gp.player.inventory[i]).getType()) {
                    case 0:
                        spriter.draw(Assets.blockTex[Items.items.get(gp.player.inventory[i]).getTex()],
                                getWidth() / 2 - Assets.invBar.getRegionWidth() / 2 + 3 + i * 20,
                                3);
                        break;
                    case 1:
                        spriter.draw(Assets.itemTex[Items.items.get(gp.player.inventory[i]).getTex()],
                                getWidth() / 2 - Assets.invBar.getRegionWidth() / 2 + 3 + i * 20,
                                3);
                        break;
                }
            }
        }
        spriter.draw(Assets.invBarCur,
                getWidth() / 2 - Assets.invBar.getRegionWidth() / 2 - 1 + 20 * gp.invSlot,
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
        if (gp.ctrlMode == 1) {
            Assets.shade.setPosition(83, getHeight() - 21);
            Assets.shade.draw(spriter);
        }
    }

    private void drawGamePlay() {
        drawWorldBackground();
        drawPlayer(gp.player);
        for (Mob mob : gp.mobs) drawMob(mob);
        for (Drop drop : gp.drops) drawDrop(drop);
        drawWorldForeground();
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
            drawString("X: " + (int) (gp.player.position.x / 16), 0, 10);
            drawString("Y: " + (int) (gp.player.position.y / 16), 0, 20);
            drawString("Mobs: " + gp.mobs.size(), 0, 30);
            drawString("Drops: " + gp.drops.size(), 0, 40);
            drawString("Block: " + Items.blocks.getKeyAt(gp.world.getForeMap(gp.curX, gp.curY)), 0, 50);
        }

        spriter.end();
    }

}
