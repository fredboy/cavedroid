package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import ru.deadsoftware.cavecraft.Assets;
import ru.deadsoftware.cavecraft.CaveGame;
import ru.deadsoftware.cavecraft.Items;
import ru.deadsoftware.cavecraft.GameScreen;
import ru.deadsoftware.cavecraft.game.objects.Player;

public class GameRenderer {

    private GameProc gameProc;

    public Vector3 camTargetPos;
    public OrthographicCamera camera;
    ShapeRenderer shapeRenderer;
    SpriteBatch spriteBatch;

    public GameRenderer(GameProc gameProc) {
        Gdx.gl.glClearColor(0f,.6f,.6f,1f);
        this.gameProc = gameProc;
        camera = new OrthographicCamera();
        camera.setToOrtho(true, 360,
                360*((float)GameScreen.getHeight()/GameScreen.getWidth()));
        camera.position.x=0;
        camera.position.y=0;
        camTargetPos = camera.position.cpy();
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setAutoShapeType(true);
        spriteBatch = new SpriteBatch();
        spriteBatch.setProjectionMatrix(camera.combined);
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
                if (gameProc.world.getForeMap(x,y)>0) {
                    spriteBatch.draw(
                            Items.BLOCKS.getValueAt(gameProc.world.getForeMap(x,y)).getTexture(),
                            x * 16 - camera.position.x,y * 16 - camera.position.y);
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

    private void drawPlayer(Player pl) {
        spriteBatch.draw(Assets.playerSkin[pl.dir],
                pl.position.x - camera.position.x, pl.position.y - camera.position.y);
    }

    private void drawGUI() {
        spriteBatch.draw(Assets.invBar, camera.viewportWidth/2 - Assets.invBar.getRegionWidth()/2,
                0);//camera.viewportHeight - Assets.invBar.getRegionHeight());
        for (int i=0; i<8; i++) {
            if (gameProc.player.inventory[i]>0) {
                spriteBatch.draw(Items.BLOCKS.getValueAt(gameProc.player.inventory[i]).getTexture(),
                        camera.viewportWidth/2 - Assets.invBar.getRegionWidth()/2+3+i*20,
                        3);
            }
        }
        spriteBatch.draw(Assets.invCur,
                camera.viewportWidth/2 - Assets.invBar.getRegionWidth()/2 - 1 + 20*gameProc.invSlot,
                -1);

        if (CaveGame.TOUCH) {
            spriteBatch.draw(Assets.touchArrows[0],26,camera.viewportHeight-52);
            spriteBatch.draw(Assets.touchArrows[1],0,camera.viewportHeight-26);
            spriteBatch.draw(Assets.touchArrows[2],26,camera.viewportHeight-26);
            spriteBatch.draw(Assets.touchArrows[3],52,camera.viewportHeight-26);
            spriteBatch.draw(Assets.touchSpace, camera.viewportWidth/2-52, camera.viewportHeight-26);
            spriteBatch.draw(Assets.touchLMB, camera.viewportWidth-52, camera.viewportHeight-26);
            spriteBatch.draw(Assets.touchRMB, camera.viewportWidth-26, camera.viewportHeight-26);
            spriteBatch.draw(Assets.touchToggleMode, 78, camera.viewportHeight-26);
            if (gameProc.ctrlMode==1) {
                Assets.shade.setPosition(83, camera.viewportHeight-21);
                Assets.shade.draw(spriteBatch);
            }
        }
    }

    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.begin();
        drawWorld();
        drawPlayer(gameProc.player);
        drawGUI();
        spriteBatch.end();

        if (gameProc.ctrlMode==1) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.ORANGE);
            shapeRenderer.set(ShapeRenderer.ShapeType.Line);
            shapeRenderer.rect(gameProc.cursorX * 16 - camera.position.x,
                    gameProc.cursorY * 16 - camera.position.y, 16, 16);
            shapeRenderer.end();
        }
    }

}
