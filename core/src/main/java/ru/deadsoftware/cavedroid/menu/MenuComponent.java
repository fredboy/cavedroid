package ru.deadsoftware.cavedroid.menu;

import dagger.Component;
import ru.deadsoftware.cavedroid.MainComponent;
import ru.fredboy.cavedroid.game.controller.container.di.ControllerContainerModule;
import ru.fredboy.cavedroid.game.controller.drop.di.ControllerDropModule;

@MenuScope
@Component(dependencies = MainComponent.class, modules = {ControllerContainerModule.class,
        ControllerDropModule.class})
public interface MenuComponent {
    MenuProc getMenuProc();
}
