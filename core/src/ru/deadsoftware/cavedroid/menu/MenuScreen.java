package ru.deadsoftware.cavedroid.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import ru.deadsoftware.cavedroid.MainConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MenuScreen implements Screen {

    private final MenuProc mMenuProc;

    @Inject
    public MenuScreen(MainConfig mainConfig) {
        MenuComponent menuComponent = DaggerMenuComponent.builder()
                .mainComponent(mainConfig.getMainComponent()).build();
        mMenuProc = menuComponent.getMenuProc();
    }

    @Override
    public void show() {
        mMenuProc.reset();
        Gdx.input.setInputProcessor(mMenuProc);
    }

    @Override
    public void render(float delta) {
        mMenuProc.render(delta);
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
