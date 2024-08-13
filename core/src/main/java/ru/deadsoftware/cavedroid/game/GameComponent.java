package ru.deadsoftware.cavedroid.game;

import dagger.Component;
import ru.deadsoftware.cavedroid.MainComponent;
import ru.deadsoftware.cavedroid.generated.module.*;
import ru.fredboy.cavedroid.common.di.GameScope;
import ru.fredboy.cavedroid.data.assets.di.DataAssetsModule;
import ru.fredboy.cavedroid.data.items.di.DataItemsModule;
import ru.fredboy.cavedroid.data.save.di.DataSaveModule;
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository;
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository;

@GameScope
@Component(dependencies = {
            MainComponent.class
        },
        modules = {GameModule.class,
//                DataAssetsModule.class,
//                DataItemsModule.class,
                DataSaveModule.class,
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

    ItemsRepository getItemsRepository();
}
