package ru.fredboy.cavedroid.gdx.menu.v2.view.stats

import ru.fredboy.cavedroid.common.api.CloudStatsSync
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.common.mvvm.ViewModelProvider
import ru.fredboy.cavedroid.domain.stats.repository.StatsRepository
import ru.fredboy.cavedroid.gdx.menu.v2.di.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class StatsMenuViewModelProvider @Inject constructor(
    private val baseViewModelDependencies: BaseViewModelDependencies,
    private val statsRepository: StatsRepository,
    private val cloudStatsSync: CloudStatsSync,
) : ViewModelProvider<StatsMenuNavKey, StatsMenuViewModel> {

    override val viewModelClass: KClass<StatsMenuViewModel>
        get() = StatsMenuViewModel::class

    override fun get(navKey: StatsMenuNavKey, navBackStack: NavBackStack): StatsMenuViewModel {
        return StatsMenuViewModel(
            navBackStack = navBackStack,
            statsRepository = statsRepository,
            cloudStatsSync = cloudStatsSync,
            baseViewModelDependencies = baseViewModelDependencies,
        )
    }
}
