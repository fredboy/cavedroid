package ru.deadsoftware.cavecraft.misc;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import ru.deadsoftware.cavecraft.GameScreen;

public abstract class Renderer {

    private OrthographicCamera camera;

    public ShapeRenderer shaper;
    public SpriteBatch spriter;

    public Renderer() {
        this(GameScreen.getWidth(), GameScreen.getHeight());
    }

    public Renderer(float width, float height) {
        camera = new OrthographicCamera();
        camera.setToOrtho(true, width, height);
        shaper = new ShapeRenderer();
        shaper.setProjectionMatrix(camera.combined);
        spriter = new SpriteBatch();
        spriter.setProjectionMatrix(camera.combined);
    }

    public float getWidth() {
        return camera.viewportWidth;
    }

    public float getHeight() {
        return camera.viewportHeight;
    }

    public float getCamX() {
        return camera.position.x;
    }

    public float getCamY() {
        return camera.position.y;
    }

    public void setCamPos(float x, float y) {
        camera.position.set(x, y, 0);
    }

    public void setFontScale(float scale) {
        Assets.mcFont.getData().setScale(scale);
    }

    public void setFontColor(int r, int g, int b) {
        Assets.mcFont.setColor(r / 255f, g / 255f, b / 255f, 1f);
    }

    public void drawString(String str, float x, float y) {
        Assets.mcFont.draw(spriter, str, x, y);
    }

    public void drawString(String str) {
        Assets.mcFont.draw(spriter, str,
                getWidth() / 2 - Assets.getStringWidth(str) / 2,
                getHeight() / 2 - Assets.getStringHeight(str) / 2);
    }

    public abstract void render();

}
