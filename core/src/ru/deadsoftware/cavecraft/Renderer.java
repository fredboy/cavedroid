package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class Renderer {

    public OrthographicCamera camera;
    public ShapeRenderer shapeRenderer;
    public SpriteBatch spriteBatch;

    public Renderer() {
    }

    public Renderer(float width, float height) {
        camera = new OrthographicCamera();
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        camera.setToOrtho(true, width, height);
        shapeRenderer.setProjectionMatrix(camera.combined);
        spriteBatch.setProjectionMatrix(camera.combined);
    }

    public float getWidth() {
        return camera.viewportWidth;
    }

    public float getHeight() {return camera.viewportHeight;}

    public abstract void render();

}
