package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import ru.deadsoftware.cavecraft.GameScreen;

public class GameRenderer {

    private GameProc gameProc;

    OrthographicCamera camera;
    ShapeRenderer shapeRenderer;

    public GameRenderer(GameProc gameProc) {
        Gdx.gl.glClearColor(0f,.8f,.8f,1f);
        this.gameProc = gameProc;
        camera = new OrthographicCamera();
        camera.setToOrtho(true, GameScreen.getWidth(), GameScreen.getHeight());
        camera.position.x=0;
        camera.position.y=0;
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);
    }

    public void drawWorld() {
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int y=0; y<gameProc.world.getHeight(); y++) {
            for (int x=0; x<gameProc.world.getWidth(); x++) {
                if (gameProc.world.getBackMap(x,y)>0) {
                    shapeRenderer.setColor(Color.DARK_GRAY);
                    shapeRenderer.rect(x*32-camera.position.x,
                            y*32-camera.position.y,32,32);
                }
                if (gameProc.world.getForeMap(x,y)>0) {
                    shapeRenderer.setColor(Color.GRAY);
                    shapeRenderer.rect(x*32-camera.position.x,
                            y*32-camera.position.y,32,32);
                }
            }
        }
        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(gameProc.cursorX*32-camera.position.x,
                gameProc.cursorY*32-camera.position.y,32,32);
        shapeRenderer.end();
    }

    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        drawWorld();
    }

}
