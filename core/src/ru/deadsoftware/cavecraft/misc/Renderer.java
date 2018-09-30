package ru.deadsoftware.cavecraft.misc;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import ru.deadsoftware.cavecraft.GameScreen;

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

    public float getHeight() {
        return camera.viewportHeight;
    }

    public void setFontScale(float scale) {
        Assets.minecraftFont.getData().setScale(scale);
    }

    public void setFontColor(int r, int g, int b) {
        Assets.minecraftFont.setColor(r / 255f, g / 255f, b / 255f, 1f);
    }

    public void drawString(String str, float x, float y) {
        Assets.minecraftFont.draw(spriteBatch, str, x, y);
    }

    public void drawString(String str) {
        Assets.minecraftFont.draw(spriteBatch, str,
                getWidth() / 2 - Assets.getStringWidth(str) / 2,
                getHeight() / 2 - Assets.getStringHeight(str) / 2);
    }

    public abstract void render();

}
