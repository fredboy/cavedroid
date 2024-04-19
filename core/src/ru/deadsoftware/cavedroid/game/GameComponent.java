package ru.deadsoftware.cavedroid.game;

import dagger.Component;
import ru.deadsoftware.cavedroid.MainComponent;
import ru.deadsoftware.cavedroid.game.actions.GameActionsModule;

@GameScope
@Component(dependencies = MainComponent.class, modules = { GameModule.class, GameActionsModule.class })
public interface GameComponent {
    GameProc getGameProc();

    GameInputProcessor getGameInputProcessor();
}
