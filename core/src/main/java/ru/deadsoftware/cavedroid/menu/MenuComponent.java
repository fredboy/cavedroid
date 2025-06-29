package ru.deadsoftware.cavedroid.menu;

import dagger.Component;
import ru.deadsoftware.cavedroid.MainComponent;
import ru.deadsoftware.cavedroid.generated.module.MenuActionsModule;
import ru.fredboy.cavedroid.common.di.MenuScope;
import ru.fredboy.cavedroid.data.menu.di.DataMenuModule;
import ru.fredboy.cavedroid.domain.menu.repository.MenuButtonRepository;
import ru.fredboy.cavedroid.game.controller.container.di.ControllerContainerModule;
import ru.fredboy.cavedroid.game.controller.drop.di.ControllerDropModule;
import ru.fredboy.cavedroid.zygote.menu.input.MenuInputProcessor;
import ru.fredboy.cavedroid.zygote.menu.renderer.MenuRenderer;
import ru.deadsoftware.cavedroid.generated.module.MenuBooleanOptionsModule;

@MenuScope
@Component(dependencies = MainComponent.class, modules = {ControllerContainerModule.class,
        ControllerDropModule.class, DataMenuModule.class, MenuBooleanOptionsModule.class, MenuActionsModule.class})
public interface MenuComponent {

    public MenuRenderer menuRenderer();

    public MenuInputProcessor menuInputProcessor();

    public MenuButtonRepository menuButtonsRepository();
}
