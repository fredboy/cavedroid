package ru.deadsoftware.cavedroid.game;

import dagger.Component;
import ru.deadsoftware.cavedroid.MainComponent;
import ru.deadsoftware.cavedroid.generated.module.*;
import ru.fredboy.cavedroid.common.di.GameScope;
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository;
import ru.fredboy.cavedroid.game.controller.container.di.ControllerContainerModule;
import ru.fredboy.cavedroid.game.controller.drop.di.ControllerDropModule;
import ru.fredboy.cavedroid.game.world.di.GameWorldModule;

@GameScope
@Component(dependencies = {
            MainComponent.class
        },
        modules = {GameModule.class,
                UseItemActionsModule.class,
                UpdateBlockActionsModule.class,
                PlaceBlockActionsModule.class,
                RenderModule.class,
                KeyboardInputHandlersModule.class,
                MouseInputHandlersModule.class,
                UseBlockActionsModule.class,
                GameWorldModule.class,
                ControllerContainerModule.class,
                ControllerDropModule.class,

        })
public interface GameComponent {
    GameProc getGameProc();

    ItemsRepository getItemsRepository();
}
