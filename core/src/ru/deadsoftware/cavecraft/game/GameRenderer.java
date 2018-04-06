package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import ru.deadsoftware.cavecraft.Assets;
import ru.deadsoftware.cavecraft.BlocksLoader;
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

    private void setColor(int r, int g, int b) {
        shapeRenderer.setColor(r/255f, g/255f, b/255f, 1f);
    }

    private void fillRect(float x, float y, float w, float h) {
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(x,y,w,h);
    }

    private void drawRect(float x, float y, float w, float h) {
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(x,y,w,h);
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
                            BlocksLoader.BLOCKS.getValueAt(gameProc.world.getForeMap(x,y)).getTexture(),
                            x * 16 - camera.position.x,y * 16 - camera.position.y);
                } else if (gameProc.world.getBackMap(x,y)>0) {
                    spriteBatch.draw(
                            BlocksLoader.BLOCKS.getValueAt(gameProc.world.getBackMap(x,y)).getTexture(),
                            x * 16 - camera.position.x,y * 16 - camera.position.y);
                    Assets.shade.setPosition(x * 16 - camera.position.x,y * 16 - camera.position.y);
                    Assets.shade.draw(spriteBatch);
                }
            }
        }
    }

    private void drawPlayer(Player pl) {
        Assets.playerSprite[pl.dir].setPosition(pl.position.x - camera.position.x,
                pl.position.y - camera.position.y);
        Assets.playerSprite[pl.dir].draw(spriteBatch);
    }

    private void drawGUI() {
        spriteBatch.draw(Assets.invBar, camera.viewportWidth/2 - Assets.invBar.getRegionWidth()/2,
                camera.viewportHeight - Assets.invBar.getRegionHeight());
        for (int i=0; i<8; i++) {
            if (gameProc.player.inventory[i]>0) {
                spriteBatch.draw(BlocksLoader.BLOCKS.getValueAt(gameProc.player.inventory[i]).getTexture(),
                        camera.viewportWidth/2 - Assets.invBar.getRegionWidth()/2+3+i*20,
                        camera.viewportHeight-19);
            }
        }
        spriteBatch.draw(Assets.invCur,
                camera.viewportWidth/2 - Assets.invBar.getRegionWidth()/2 - 1 + 20*gameProc.invSlot,
                camera.viewportHeight - Assets.invBar.getRegionHeight() - 2);
    }

    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        spriteBatch.begin();
        drawWorld();
        drawPlayer(gameProc.player);
        drawGUI();
        spriteBatch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.ORANGE);
        drawRect(gameProc.cursorX*16-camera.position.x,
                gameProc.cursorY*16-camera.position.y,16,16);
        shapeRenderer.end();
    }

}
