package ru.deadsoftware.cavedroid.misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class Renderer implements InputProcessor {

    protected final ShapeRenderer shaper;
    protected final SpriteBatch spriter;
    private final OrthographicCamera camera;

    protected Renderer() {
        this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    protected Renderer(float width, float height) {
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

    public abstract void render(float delta);

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

}
