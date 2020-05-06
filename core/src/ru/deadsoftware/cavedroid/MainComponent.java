package ru.deadsoftware.cavedroid;

import dagger.Component;
import ru.deadsoftware.cavedroid.game.GameScreen;
import ru.deadsoftware.cavedroid.menu.MenuScreen;

import javax.inject.Singleton;

@Singleton
@Component(dependencies = CaveGame.class)
public interface MainComponent {
    GameScreen getGameScreen();

    MenuScreen getMenuScreen();

    MainConfig getMainConfig();
}
