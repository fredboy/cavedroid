package ru.fredboy.cavedroid.gdx.menu.option.annotation

import ru.fredboy.automultibind.annotations.BindsIntoMapStringKey
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig
import ru.fredboy.cavedroid.gdx.menu.option.bool.IMenuBooleanOption

@BindsIntoMapStringKey(
    interfaceClass = IMenuBooleanOption::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "MenuBooleanOptionsModule",
)
annotation class BindsMenuBooleanOption(val stringKey: String)
