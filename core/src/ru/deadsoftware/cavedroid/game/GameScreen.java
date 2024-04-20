package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import ru.deadsoftware.cavedroid.MainConfig;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GameScreen implements Screen {

    private final MainConfig mMainConfig;

    @CheckForNull
    private GameProc mGameProc;
    @CheckForNull
    private GameInputProcessor mGameInputProcessor;
    @CheckForNull
    private GameItemsHolder mGameItemsHolder;

    @Inject
    public GameScreen(MainConfig mainConfig) {
        mMainConfig = mainConfig;
    }

    public void newGame(int gameMode) {
        if (mGameProc != null) {
            mGameProc.dispose();
        }

        GameModule.loaded = true;

        GameComponent gameComponent = DaggerGameComponent.builder()
                .mainComponent(mMainConfig.getMainComponent()).build();

        mGameProc = gameComponent.getGameProc();
        mGameInputProcessor = gameComponent.getGameInputProcessor();

        mGameProc.setPlayerGameMode(gameMode);

        Gdx.input.setInputProcessor(gameComponent.getGameInputProcessor());
    }

    public void loadGame() {
        if (mGameProc != null) {
            mGameProc.dispose();
        }

        GameModule.loaded = false;

        GameComponent gameComponent = DaggerGameComponent.builder()
                .mainComponent(mMainConfig.getMainComponent()).build();

        mGameProc = gameComponent.getGameProc();
        mGameInputProcessor = gameComponent.getGameInputProcessor();

        Gdx.input.setInputProcessor(gameComponent.getGameInputProcessor());
    }

    @Override
    public void render(float delta) {
        mGameProc.update(delta);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(mGameInputProcessor);
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
        if (mGameProc != null) {
            mGameProc.dispose();
        }
    }

}
