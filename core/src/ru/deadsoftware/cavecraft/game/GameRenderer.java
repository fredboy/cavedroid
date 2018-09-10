package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.CaveGame;
import ru.deadsoftware.cavecraft.GameScreen;
import ru.deadsoftware.cavecraft.game.mobs.Mob;
import ru.deadsoftware.cavecraft.game.objects.Player;
import ru.deadsoftware.cavecraft.misc.Assets;
import ru.deadsoftware.cavecraft.misc.Renderer;

public class GameRenderer extends Renderer {

    private GameProc gameProc;

    public GameRenderer(GameProc gameProc,float width, float heigth) {
        super(width,heigth);
        Gdx.gl.glClearColor(0f,.6f,.6f,1f);
        this.gameProc = gameProc;
    }

    private void drawWorldBackground() {
        int minX = (int) (camera.position.x/16)-1;
        int minY = (int) (camera.position.y/16)-1;
        int maxX = (int) ((camera.position.x+camera.viewportWidth)/16)+1;
        int maxY = (int) ((camera.position.y+camera.viewportHeight)/16)+1;
        if (minY<0) minY=0;
        if (maxY>gameProc.world.getHeight()) maxY = gameProc.world.getHeight();
        for (int y=minY; y<maxY; y++) {
            for (int x=minX; x<maxX; x++) {
                if ((gameProc.world.getForeMap(x,y)==0 || Items.BLOCKS.getValueAt(gameProc.world.getForeMap(x,y)).transparent)
                        && gameProc.world.getBackMap(x,y)>0) {
                    spriteBatch.draw(
                            Assets.blockTextures[Items.BLOCKS.getValueAt(gameProc.world.getBackMap(x,y)).getTexture()],
                            x * 16 - camera.position.x,y * 16 - camera.position.y);
                    Assets.shade.setPosition(x * 16 - camera.position.x,y * 16 - camera.position.y);
                    Assets.shade.draw(spriteBatch);
                }
                if (gameProc.world.getForeMap(x,y)>0 && Items.BLOCKS.getValueAt(gameProc.world.getForeMap(x,y)).background) {
                    spriteBatch.draw(
                            Assets.blockTextures[Items.BLOCKS.getValueAt(gameProc.world.getForeMap(x,y)).getTexture()],
                            x * 16 - camera.position.x,y * 16 - camera.position.y);
                }
            }
        }
    }

    private void drawWorldForeground(){
        int minX = (int) (camera.position.x/16)-1;
        int minY = (int) (camera.position.y/16)-1;
        int maxX = (int) ((camera.position.x+camera.viewportWidth)/16)+1;
        int maxY = (int) ((camera.position.y+camera.viewportHeight)/16)+1;
        if (minY<0) minY=0;
        if (maxY>gameProc.world.getHeight()) maxY = gameProc.world.getHeight();
        for (int y=minY; y<maxY; y++) {
            for (int x=minX; x<maxX; x++) {
                if (gameProc.world.getForeMap(x,y)>0 && !Items.BLOCKS.getValueAt(gameProc.world.getForeMap(x,y)).background) {
                    spriteBatch.draw(
                            Assets.blockTextures[Items.BLOCKS.getValueAt(gameProc.world.getForeMap(x,y)).getTexture()],
                            x * 16 - camera.position.x,y * 16 - camera.position.y);
                }
            }
        }
    }

    private void drawMob(Mob mob) {
        mob.draw(spriteBatch,
                mob.position.x-camera.position.x-gameProc.world.getWidth()*16, mob.position.y-camera.position.y);
        mob.draw(spriteBatch,
                mob.position.x-camera.position.x, mob.position.y-camera.position.y);
        mob.draw(spriteBatch,
                mob.position.x-camera.position.x+gameProc.world.getWidth()*16, mob.position.y-camera.position.y);
    }

    private void drawPlayer(Player pl) {
        if (!pl.moveX.equals(Vector2.Zero) || Assets.playerSprite[0][2].getRotation()!=0) {
            Assets.playerSprite[0][2].rotate(Player.ANIM_SPEED);
            Assets.playerSprite[1][2].rotate(-Player.ANIM_SPEED);
            Assets.playerSprite[0][3].rotate(-Player.ANIM_SPEED);
            Assets.playerSprite[1][3].rotate(Player.ANIM_SPEED);
        } else {
            Assets.playerSprite[0][2].setRotation(0);
            Assets.playerSprite[1][2].setRotation(0);
            Assets.playerSprite[0][3].setRotation(0);
            Assets.playerSprite[1][3].setRotation(0);
        }
        if (Assets.playerSprite[0][2].getRotation()>=60 || Assets.playerSprite[0][2].getRotation()<=-60)
            Player.ANIM_SPEED = -Player.ANIM_SPEED;

        //back hand
        Assets.playerSprite[1][2].setPosition(
                pl.position.x - camera.position.x - 6,
                pl.position.y - camera.position.y);
        Assets.playerSprite[1][2].draw(spriteBatch);
        //back leg
        Assets.playerSprite[1][3].setPosition(
                pl.position.x - camera.position.x - 6,
                pl.position.y - camera.position.y + 10);
        Assets.playerSprite[1][3].draw(spriteBatch);
        //front leg
        Assets.playerSprite[0][3].setPosition(
                pl.position.x - camera.position.x - 6,
                pl.position.y - camera.position.y + 10);
        Assets.playerSprite[0][3].draw(spriteBatch);
        //head
        spriteBatch.draw(Assets.playerSprite[pl.dir][0],
                pl.position.x - camera.position.x - 2,
                pl.position.y - camera.position.y - 2);
        //body
        spriteBatch.draw(Assets.playerSprite[pl.dir][1],
                pl.position.x - camera.position.x - 2, pl.position.y - camera.position.y + 8);
        //front hand
        Assets.playerSprite[0][2].setPosition(
                pl.position.x - camera.position.x - 6,
                pl.position.y - camera.position.y);
        Assets.playerSprite[0][2].draw(spriteBatch);
    }

    private void drawCreative() {
        float x = camera.viewportWidth/2-Assets.creativeInv.getRegionWidth()/2;
        float y = camera.viewportHeight/2-Assets.creativeInv.getRegionHeight()/2;
        spriteBatch.draw(Assets.creativeInv, x, y);
        spriteBatch.draw(Assets.creativeScroll, x+156,
                y+18+(gameProc.creativeScroll*(72/gameProc.maxCreativeScroll)));
        for (int i=gameProc.creativeScroll*8; i<gameProc.creativeScroll*8+40; i++) {
            if (i>0 && i<Items.BLOCKS.size)
                spriteBatch.draw(Assets.blockTextures[Items.BLOCKS.getValueAt(i).getTexture()],
                        x+8+((i-gameProc.creativeScroll*8)%8)*18,
                        y+18+((i-gameProc.creativeScroll*8)/8)*18);
        }
        for (int i=0; i<9; i++) {
            if (gameProc.player.inventory[i]>0)
                spriteBatch.draw(Assets.blockTextures[Items.BLOCKS.getValueAt(gameProc.player.inventory[i]).getTexture()],
                        x+8+i*18, y+Assets.creativeInv.getRegionHeight()-24);
        }
    }

    private void drawGUI() {
        if (gameProc.world.getForeMap(gameProc.cursorX, gameProc.cursorY)>0 ||
                gameProc.world.getBackMap(gameProc.cursorX, gameProc.cursorY)>0 ||
                gameProc.ctrlMode==1 ||
                !CaveGame.TOUCH)
            spriteBatch.draw(Assets.guiCur,
                    gameProc.cursorX*16-camera.position.x,
                    gameProc.cursorY*16-camera.position.y);
        spriteBatch.draw(Assets.invBar, camera.viewportWidth/2 - Assets.invBar.getRegionWidth()/2, 0);
        for (int i=0; i<9; i++) {
            if (gameProc.player.inventory[i]>0) {
                spriteBatch.draw(Assets.blockTextures[Items.BLOCKS.getValueAt(gameProc.player.inventory[i]).getTexture()],
                        camera.viewportWidth/2 - Assets.invBar.getRegionWidth()/2+3+i*20,
                        3);
            }
        }
        spriteBatch.draw(Assets.invBarCur,
                camera.viewportWidth/2 - Assets.invBar.getRegionWidth()/2 - 1 + 20*gameProc.invSlot,
                -1);
    }

    private void drawTouchGui() {
        spriteBatch.draw(Assets.touchArrows[0],26,camera.viewportHeight-52);
        spriteBatch.draw(Assets.touchArrows[1],0,camera.viewportHeight-26);
        spriteBatch.draw(Assets.touchArrows[2],26,camera.viewportHeight-26);
        spriteBatch.draw(Assets.touchArrows[3],52,camera.viewportHeight-26);
        spriteBatch.draw(Assets.touchLMB, camera.viewportWidth-52, camera.viewportHeight-26);
        spriteBatch.draw(Assets.touchRMB, camera.viewportWidth-26, camera.viewportHeight-26);
        spriteBatch.draw(Assets.touchToggleMode, 78, camera.viewportHeight-26);
        if (gameProc.ctrlMode==1) {
            Assets.shade.setPosition(83, camera.viewportHeight-21);
            Assets.shade.draw(spriteBatch);
        }
    }

    private void drawGamePlay() {
        drawWorldBackground();
        drawPlayer(gameProc.player);
        for (Mob mob : gameProc.mobs) drawMob(mob);
        drawWorldForeground();
        drawGUI();
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.begin();
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
            drawString("FPS: "+GameScreen.FPS,0, 0);
            drawString("X: "+(int)(gameProc.player.position.x/16),0, 10);
            drawString("Y: "+(int)(gameProc.player.position.y/16),0, 20);
            drawString("Mobs: "+gameProc.mobs.size(), 0, 30);
            drawString("Block: "+Items.BLOCKS.getKeyAt(gameProc.world.getForeMap(gameProc.cursorX, gameProc.cursorY)), 0, 40);
        }

        spriteBatch.end();
    }

}
