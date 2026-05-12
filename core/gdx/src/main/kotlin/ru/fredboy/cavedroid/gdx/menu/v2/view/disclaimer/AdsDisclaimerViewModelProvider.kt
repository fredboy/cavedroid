package ru.fredboy.cavedroid.gdx.menu.v2.view.disclaimer

import ru.fredboy.cavedroid.common.api.AdController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.common.mvvm.ViewModelProvider
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.menu.v2.di.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class AdsDisclaimerViewModelProvider @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val adController: AdController,
    private val baseViewModelDependencies: BaseViewModelDependencies,
) : ViewModelProvider<AdsDisclaimerNavKey, AdsDisclaimerViewModel> {

    override val viewModelClass: KClass<AdsDisclaimerViewModel>
        get() = AdsDisclaimerViewModel::class

    override fun get(navKey: AdsDisclaimerNavKey, navBackStack: NavBackStack): AdsDisclaimerViewModel {
        return AdsDisclaimerViewModel(
            navBackStack = navBackStack,
            applicationContextRepository = applicationContextRepository,
            adController = adController,
            baseViewModelDependencies = baseViewModelDependencies,
        )
    }
}
