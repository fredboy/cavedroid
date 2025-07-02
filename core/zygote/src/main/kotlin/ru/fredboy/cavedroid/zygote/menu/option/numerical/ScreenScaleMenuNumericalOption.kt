package ru.fredboy.cavedroid.zygote.menu.option.numerical

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.zygote.menu.option.annotation.BindsMenuNumericalOption
import javax.inject.Inject

@MenuScope
@BindsMenuNumericalOption(ScreenScaleMenuNumericalOption.KEY)
class ScreenScaleMenuNumericalOption @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val applicationController: ApplicationController,
) : IMenuNumericalOption {

    override fun getOption(): Number {
        return applicationContextRepository.getScreenScale()
    }

    override fun setNextOption() {
        val nextIndex = (SCALE_VALUES.indexOf(getOption()) + 1) % SCALE_VALUES.size
        val nextValue = SCALE_VALUES[nextIndex]
        applicationContextRepository.setScreenScale(nextValue)
        applicationController.triggerResize()
    }
    companion object {
        const val KEY = "screen_scale"
        private val SCALE_VALUES = arrayOf(1, 2, 3, 4, 5, 6)
    }
}
