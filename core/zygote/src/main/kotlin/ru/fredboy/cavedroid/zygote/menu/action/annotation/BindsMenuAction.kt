package ru.fredboy.cavedroid.zygote.menu.action.annotation

import ru.fredboy.automultibind.annotations.BindsIntoMapStringKey
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig
import ru.fredboy.cavedroid.zygote.menu.action.IMenuAction

@BindsIntoMapStringKey(
    interfaceClass = IMenuAction::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "MenuActionsModule",
)
annotation class BindsMenuAction(val stringKey: String)
