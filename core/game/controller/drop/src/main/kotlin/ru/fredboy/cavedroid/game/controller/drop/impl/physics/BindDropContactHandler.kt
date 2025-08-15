package ru.fredboy.cavedroid.game.controller.drop.impl.physics

import ru.fredboy.automultibind.annotations.BindsIntoSet
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig
import ru.fredboy.cavedroid.domain.world.abstraction.AbstractContactHandler

@BindsIntoSet(
    interfaceClass = AbstractContactHandler::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "DropContactHandlerModule",
)
annotation class BindDropContactHandler
