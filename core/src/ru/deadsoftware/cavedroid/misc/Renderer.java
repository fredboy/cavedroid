package ru.deadsoftware.cavedroid.misc;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import ru.deadsoftware.cavedroid.GameScreen;

public abstract class Renderer {

    private OrthographicCamera camera;

    protected ShapeRenderer shaper;
    protected SpriteBatch spriter;

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
        Assets.minecraftFont.getData().setScale(scale);
    }

    protected void setFontColor(int r, int g, int b) {
        Assets.minecraftFont.setColor(r / 255f, g / 255f, b / 255f, 1f);
    }

    protected void drawString(String str, float x, float y) {
        Assets.minecraftFont.draw(spriter, str, x, y);
    }

    protected void drawString(String str) {
        Assets.minecraftFont.draw(spriter, str,
                getWidth() / 2 - (float) Assets.getStringWidth(str) / 2,
                getHeight() / 2 - (float) Assets.getStringHeight(str) / 2);
    }

    public abstract void render();

}
