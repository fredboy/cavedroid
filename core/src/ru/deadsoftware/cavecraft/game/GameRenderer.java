package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import ru.deadsoftware.cavecraft.GameScreen;

public class GameRenderer {

    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;

    public GameRenderer() {
        Gdx.gl.glClearColor(0f,.8f,.8f,1f);
    }

    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    }

}
