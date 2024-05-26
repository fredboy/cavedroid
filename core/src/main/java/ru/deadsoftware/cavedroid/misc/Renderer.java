package ru.deadsoftware.cavedroid.misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public abstract class Renderer implements InputProcessor {

    protected final ShapeRenderer shaper;
    protected final SpriteBatch spriter;
    private final OrthographicCamera camera;
    private final Rectangle mCameraViewport;

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

        mCameraViewport =
                new Rectangle(camera.position.x, camera.position.y, camera.viewportWidth, camera.viewportHeight);
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
        mCameraViewport.x = x;
        mCameraViewport.y = y;
    }

    public Rectangle getCameraViewport() {
        return mCameraViewport;
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

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }
}
