package ru.fredboy.cavedroid.gdx.game.window

import ru.fredboy.automultibind.annotations.BindsIntoMapStringKey
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig

@BindsIntoMapStringKey(
    interfaceClass = GameWindowFactory::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "GameWindowFactoriesModule",
)
annotation class BindGameWindow(val stringKey: String)
