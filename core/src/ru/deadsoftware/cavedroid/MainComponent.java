package ru.deadsoftware.cavedroid;

import dagger.Component;
import ru.deadsoftware.cavedroid.game.GameScreen;

import javax.inject.Singleton;

@Singleton
@Component
public interface MainComponent {
    GameScreen getGameScreen();
    MainConfig getGameConfig();
}
