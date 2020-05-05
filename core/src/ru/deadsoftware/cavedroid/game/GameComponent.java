package ru.deadsoftware.cavedroid.game;

import dagger.Component;
import ru.deadsoftware.cavedroid.MainComponent;

@GameScope
@Component(dependencies = MainComponent.class)
public interface GameComponent {
    GameProc getGameProc();
    GameInputProcessor getGameInputProcessor();
}
