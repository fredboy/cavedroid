package ru.deadsoftware.cavedroid.menu;

import dagger.Component;
import ru.deadsoftware.cavedroid.MainComponent;

@MenuScope
@Component(dependencies = MainComponent.class)
public interface MenuComponent {
    MenuProc getMenuProc();
}
