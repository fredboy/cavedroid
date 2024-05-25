package ru.deadsoftware.cavedroid.game;

import dagger.Component;
import ru.deadsoftware.cavedroid.MainComponent;
import ru.deadsoftware.cavedroid.generated.module.*;

@GameScope
@Component(dependencies = MainComponent.class,
        modules = {GameModule.class,
                UseItemActionsModule.class,
                UpdateBlockActionsModule.class,
                PlaceBlockActionsModule.class,
                RenderModule.class,
                KeyboardInputHandlersModule.class,
                MouseInputHandlersModule.class,
                UseBlockActionsModule.class
        })
public interface GameComponent {
    GameProc getGameProc();

    GameItemsHolder getGameItemsHolder();
}
