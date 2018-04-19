package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.Assets;
import ru.deadsoftware.cavecraft.CaveGame;
import ru.deadsoftware.cavecraft.Items;
import ru.deadsoftware.cavecraft.GameScreen;
import ru.deadsoftware.cavecraft.game.mobs.Mob;
import ru.deadsoftware.cavecraft.game.objects.Player;

public class GameRenderer {

    private GameProc gameProc;

    public OrthographicCamera camera, fontCam;
    ShapeRenderer shapeRenderer;
    SpriteBatch spriteBatch, fontBatch;

    public GameRenderer(GameProc gameProc) {
        Gdx.gl.glClearColor(0f,.6f,.6f,1f);
        this.gameProc = gameProc;
        camera = new OrthographicCamera();
        if (!CaveGame.TOUCH) {
            camera.setToOrtho(true, 480,
                    480 * ((float) GameScreen.getHeight() / GameScreen.getWidth()));
        } else {
            camera.setToOrtho(true, 320,
                    320 * ((float) GameScreen.getHeight() / GameScreen.getWidth()));
        }
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setAutoShapeType(true);
        spriteBatch = new SpriteBatch();
        spriteBatch.setProjectionMatrix(camera.combined);

        fontCam = new OrthographicCamera();
        fontCam.setToOrtho(true, GameScreen.getWidth(), GameScreen.getHeight());
        fontBatch = new SpriteBatch();
        fontBatch.setProjectionMatrix(fontCam.combined);
    }

    private void setFontColor(int r, int g, int b) {
        Assets.minecraftFont.setColor(r/255f, g/255f, b/255f, 1f);
    }

    private void drawString(String str, float x, float y) {
        Assets.minecraftFont.draw(fontBatch, str, x, y);
    }

    private void drawWorld() {
        int minX = (int) (camera.position.x/16);
        int minY = (int) (camera.position.y/16);
        int maxX = (int) ((camera.position.x+camera.viewportWidth)/16)+1;
        int maxY = (int) ((camera.position.y+camera.viewportHeight)/16)+1;
        if (minX<0) minX=0;
        if (minY<0) minY=0;
        if (maxX>gameProc.world.getWidth()) maxX = gameProc.world.getWidth();
        if (maxY>gameProc.world.getHeight()) maxY = gameProc.world.getHeight();
        for (int y=minY; y<maxY; y++) {
            for (int x=minX; x<maxX; x++) {
                if (gameProc.world.getForeMap(x,y)>0){/* &&
                        !Items.BLOCKS.getValueAt(gameProc.world.getForeMap(x,y)).foreground) {
                    spriteBatch.draw(
                            Items.BLOCKS.getValueAt(gameProc.world.getForeMap(x,y)).getTexture(),
                            x * 16 - camera.position.x,y * 16 - camera.position.y);*/
                } else if (gameProc.world.getBackMap(x,y)>0) {
                    spriteBatch.draw(
                            Items.BLOCKS.getValueAt(gameProc.world.getBackMap(x,y)).getTexture(),
                            x * 16 - camera.position.x,y * 16 - camera.position.y);
                    Assets.shade.setPosition(x * 16 - camera.position.x,y * 16 - camera.position.y);
                    Assets.shade.draw(spriteBatch);
                }
            }
        }
    }

    private void drawWorldForeground(){
        int minX = (int) (camera.position.x/16);
        int minY = (int) (camera.position.y/16);
        int maxX = (int) ((camera.position.x+camera.viewportWidth)/16)+1;
        int maxY = (int) ((camera.position.y+camera.viewportHeight)/16)+1;
        if (minX<0) minX=0;
        if (minY<0) minY=0;
        if (maxX>gameProc.world.getWidth()) maxX = gameProc.world.getWidth();
        if (maxY>gameProc.world.getHeight()) maxY = gameProc.world.getHeight();
        for (int y=minY; y<maxY; y++) {
            for (int x=minX; x<maxX; x++) {
                if (gameProc.world.getForeMap(x,y)>0) { /*&&
                        Items.BLOCKS.getValueAt(gameProc.world.getForeMap(x,y)).foreground) {*/
                    spriteBatch.draw(
                            Items.BLOCKS.getValueAt(gameProc.world.getForeMap(x,y)).getTexture(),
                            x * 16 - camera.position.x,y * 16 - camera.position.y);
                }
            }
        }
    }

    private void drawMob(Mob mob) {
        mob.draw(spriteBatch,
                mob.position.x-camera.position.x, mob.position.y-camera.position.y);
    }

    private void drawPlayer(Player pl) {
        if (!pl.moveX.equals(Vector2.Zero) || Assets.playerSkin[0][2].getRotation()!=0) {
            Assets.playerSkin[0][2].rotate(Mob.ANIM_SPEED);
            Assets.playerSkin[1][2].rotate(-Mob.ANIM_SPEED);
            Assets.playerSkin[0][3].rotate(-Mob.ANIM_SPEED);
            Assets.playerSkin[1][3].rotate(Mob.ANIM_SPEED);
        } else {
            Assets.playerSkin[0][2].setRotation(0);
            Assets.playerSkin[1][2].setRotation(0);
            Assets.playerSkin[0][3].setRotation(0);
            Assets.playerSkin[1][3].setRotation(0);
        }
        if (Assets.playerSkin[0][2].getRotation()>=60 || Assets.playerSkin[0][2].getRotation()<=-60)
            Mob.ANIM_SPEED = -Mob.ANIM_SPEED;

        //back hand
        Assets.playerSkin[1][2].setPosition(
                pl.position.x - camera.position.x - 6,
                pl.position.y - camera.position.y);
        Assets.playerSkin[1][2].draw(spriteBatch);
        //back leg
        Assets.playerSkin[1][3].setPosition(
                pl.position.x - camera.position.x - 6,
                pl.position.y - camera.position.y + 10);
        Assets.playerSkin[1][3].draw(spriteBatch);
        //front leg
        Assets.playerSkin[0][3].setPosition(
                pl.position.x - camera.position.x - 6,
                pl.position.y - camera.position.y + 10);
        Assets.playerSkin[0][3].draw(spriteBatch);
        //head
        spriteBatch.draw(Assets.playerSkin[pl.dir][0],
                pl.position.x - camera.position.x - 2,
                pl.position.y - camera.position.y - 2);
        //body
        spriteBatch.draw(Assets.playerSkin[pl.dir][1],
                pl.position.x - camera.position.x - 2, pl.position.y - camera.position.y + 8);
        //front hand
        Assets.playerSkin[0][2].setPosition(
                pl.position.x - camera.position.x - 6,
                pl.position.y - camera.position.y);
        Assets.playerSkin[0][2].draw(spriteBatch);
    }

    private void drawCreative() {
        float x = camera.viewportWidth/2-Assets.creativeInv.getRegionWidth()/2;
        float y = camera.viewportHeight/2-Assets.creativeInv.getRegionHeight()/2;
        spriteBatch.draw(Assets.creativeInv, x, y);
        spriteBatch.draw(Assets.creativeScroll, x+156, y+18);
        for (int i=1; i<Items.BLOCKS.size; i++) {
            spriteBatch.draw(Items.BLOCKS.getValueAt(i).getTexture(),x+8+(i%8)*18,
                    y+18+(i/8)*18);
        }
        for (int i=0; i<9; i++) {
            if (gameProc.player.inventory[i]>0)
                spriteBatch.draw(Items.BLOCKS.getValueAt(gameProc.player.inventory[i]).getTexture(),
                        x+8+i*18, y+184);
        }
    }

    private void drawGUI() {
        if (gameProc.world.getForeMap(gameProc.cursorX, gameProc.cursorY)>0 ||
                gameProc.world.getBackMap(gameProc.cursorX, gameProc.cursorY)>0 ||
                gameProc.ctrlMode==1)
            spriteBatch.draw(Assets.guiCur,
                    gameProc.cursorX*16-camera.position.x,
                    gameProc.cursorY*16-camera.position.y);
        spriteBatch.draw(Assets.invBar, camera.viewportWidth/2 - Assets.invBar.getRegionWidth()/2, 0);
        for (int i=0; i<9; i++) {
            if (gameProc.player.inventory[i]>0) {
                spriteBatch.draw(Items.BLOCKS.getValueAt(gameProc.player.inventory[i]).getTexture(),
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
    }

    private void drawGamePlay() {
        drawWorld();
        for (Mob mob : gameProc.mobs) drawMob(mob);
        drawPlayer(gameProc.player);
        drawWorldForeground();
        drawGUI();
    }

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
        spriteBatch.end();

        if (CaveGame.TOUCH) {
            spriteBatch.begin();
            drawTouchGui();
            spriteBatch.end();
        }

        fontBatch.begin();
        setFontColor(255,255,255);
        drawString("CaveCraft "+CaveGame.VERSION, 0, 0);
        drawString("FPS: "+GameScreen.FPS, 0, 20);
        drawString("X: "+(int)(gameProc.player.position.x/16), 0, 40);
        drawString("Y: "+(int)(gameProc.player.position.y/16), 0, 60);
        fontBatch.end();
    }

}
