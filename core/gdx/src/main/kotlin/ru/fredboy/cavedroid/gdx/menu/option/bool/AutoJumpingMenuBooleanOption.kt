package ru.fredboy.cavedroid.gdx.menu.option.bool

import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.menu.option.annotation.BindsMenuBooleanOption
import javax.inject.Inject

@MenuScope
@BindsMenuBooleanOption(AutoJumpingMenuBooleanOption.KEY)
class AutoJumpingMenuBooleanOption @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
) : IMenuBooleanOption {

    override fun getOption(): Boolean = applicationContextRepository.isAutoJumpEnabled()

    override fun toggleOption() {
        applicationContextRepository.setAutoJumpEnabled(!getOption())
    }

    companion object {
        const val KEY = "auto_jump"
    }
}
