package ru.deadsoftware.cavedroid.menu;

import dagger.Component;
import ru.deadsoftware.cavedroid.MainComponent;
import ru.fredboy.cavedroid.data.save.di.DataSaveModule;

@MenuScope
@Component(dependencies = MainComponent.class, modules = DataSaveModule.class)
public interface MenuComponent {
    MenuProc getMenuProc();
}
