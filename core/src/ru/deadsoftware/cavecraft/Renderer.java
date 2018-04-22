package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class Renderer {

    public OrthographicCamera camera;
    public ShapeRenderer shapeRenderer;
    public SpriteBatch spriteBatch;

    public Renderer() {
        this(GameScreen.getWidth(), GameScreen.getHeight());
    }

    public Renderer(float width, float height) {
        camera = new OrthographicCamera();
        camera.setToOrtho(true, width, height);
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);
        spriteBatch = new SpriteBatch();
        spriteBatch.setProjectionMatrix(camera.combined);
    }

    public float getWidth() {
        return camera.viewportWidth;
    }

    public float getHeight() {return camera.viewportHeight;}

    public void setFontScale(float scale) {
        Assets.minecraftFont.getData().setScale(scale);
    }

    public void setFontColor(int r, int g, int b) {
        Assets.minecraftFont.setColor(r/255f, g/255f, b/255f, 1f);
    }

    public void drawString(String str, float x, float y) {
        Assets.minecraftFont.draw(spriteBatch, str, x, y);
    }

    public abstract void render();

}
