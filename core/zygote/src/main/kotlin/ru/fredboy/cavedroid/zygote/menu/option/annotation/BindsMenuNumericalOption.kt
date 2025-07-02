package ru.fredboy.cavedroid.zygote.menu.option.annotation

import ru.fredboy.automultibind.annotations.BindsIntoMapStringKey
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig
import ru.fredboy.cavedroid.zygote.menu.option.numerical.IMenuNumericalOption

@BindsIntoMapStringKey(
    interfaceClass = IMenuNumericalOption::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "MenuNumericalOptionsModule",
)
annotation class BindsMenuNumericalOption(val stringKey: String)
