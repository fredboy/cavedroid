package ru.deadsoftware.cavedroid.game;

import dagger.Component;
import ru.deadsoftware.cavedroid.MainComponent;
import ru.deadsoftware.cavedroid.game.actions.UpdateBlockActionsModule;
import ru.deadsoftware.cavedroid.game.actions.UseItemActionsModule;

@GameScope
@Component(dependencies = MainComponent.class, modules = { GameModule.class, UseItemActionsModule.class, UpdateBlockActionsModule.class })
public interface GameComponent {
    GameProc getGameProc();

    GameInputProcessor getGameInputProcessor();
}
