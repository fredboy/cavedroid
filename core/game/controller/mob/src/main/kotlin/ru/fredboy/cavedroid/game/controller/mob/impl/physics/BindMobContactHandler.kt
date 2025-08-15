package ru.fredboy.cavedroid.game.controller.mob.impl.physics

import ru.fredboy.automultibind.annotations.BindsIntoSet
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig
import ru.fredboy.cavedroid.domain.world.abstraction.AbstractContactHandler

@BindsIntoSet(
    interfaceClass = AbstractContactHandler::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "MobContactHandlerModule",
)
annotation class BindMobContactHandler
