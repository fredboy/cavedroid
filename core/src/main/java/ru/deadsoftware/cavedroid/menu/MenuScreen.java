package ru.deadsoftware.cavedroid.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.fredboy.cavedroid.domain.menu.repository.MenuButtonRepository;
import ru.fredboy.cavedroid.zygote.menu.input.MenuInputProcessor;
import ru.fredboy.cavedroid.zygote.menu.renderer.MenuRenderer;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MenuScreen implements Screen {

    private final MenuRenderer mMenuRenderer;

    private final MenuInputProcessor mMenuInputProcessor;

    private final MenuButtonRepository mMenuButtonsRepository;

    @Inject
    public MenuScreen(MainConfig mainConfig) {
        MenuComponent menuComponent = DaggerMenuComponent.builder()
                .mainComponent(mainConfig.getMainComponent()).build();

        mMenuRenderer = menuComponent.menuRenderer();
        mMenuInputProcessor = menuComponent.menuInputProcessor();
        mMenuButtonsRepository = menuComponent.menuButtonsRepository();
    }

    public void resetMenu() {
        mMenuButtonsRepository.setCurrentMenu("main");
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(mMenuInputProcessor);
    }

    @Override
    public void render(float delta) {
        mMenuRenderer.render(delta);
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
