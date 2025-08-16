package ru.fredboy.cavedroid.gdx.menu.option.bool

import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.menu.option.annotation.BindsMenuBooleanOption
import javax.inject.Inject

@MenuScope
@BindsMenuBooleanOption(DynamicCameraMenuBooleanOption.KEY)
class DynamicCameraMenuBooleanOption @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
) : IMenuBooleanOption {

    override fun getOption(): Boolean = applicationContextRepository.useDynamicCamera()

    override fun toggleOption() {
        applicationContextRepository.setUseDynamicCamera(!getOption())
    }

    companion object {
        const val KEY = "dyncam"
    }
}
