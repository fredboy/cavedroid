package ru.fredboy.cavedroid.zygote.menu.option.bool

import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.zygote.menu.option.annotation.BindsMenuBooleanOption
import javax.inject.Inject

@MenuScope
@BindsMenuBooleanOption(FullscreenMenuBooleanOption.KEY)
class FullscreenMenuBooleanOption @Inject constructor(
    private val gameContextRepository: GameContextRepository,
) : IMenuBooleanOption {

    override fun getOption(): Boolean {
        return gameContextRepository.isFullscreen()
    }

    override fun toggleOption() {
        gameContextRepository.setFullscreen(!getOption())
    }

    companion object {
        const val KEY = "fullscreen"
    }
}