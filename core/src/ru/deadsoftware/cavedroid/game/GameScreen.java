package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import ru.deadsoftware.cavedroid.MainConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GameScreen implements Screen {

    private static final String TAG = "GameScreen";

    private final MainConfig mMainConfig;

    private GameProc mGameProc;

    @Inject
    public GameScreen(MainConfig mainConfig) {
        mMainConfig = mainConfig;
        newGame();
    }

    public void newGame() {
        GameComponent gameComponent = DaggerGameComponent.builder()
                .mainComponent(mMainConfig.getMainComponent()).build();
        mGameProc = gameComponent.getGameProc();
        Gdx.input.setInputProcessor(gameComponent.getGameInputProcessor());
    }

    @Override
    public void render(float delta) {
        mGameProc.update(delta);
    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
    }

}
