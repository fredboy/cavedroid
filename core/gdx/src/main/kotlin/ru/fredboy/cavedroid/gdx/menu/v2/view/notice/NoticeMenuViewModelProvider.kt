package ru.fredboy.cavedroid.gdx.menu.v2.view.notice

import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.common.mvvm.ViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.di.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class NoticeMenuViewModelProvider @Inject constructor(
    private val baseViewModelDependencies: BaseViewModelDependencies,
) : ViewModelProvider<NoticeMenuNavKey, NoticeMenuViewModel> {

    override val viewModelClass: KClass<NoticeMenuViewModel>
        get() = NoticeMenuViewModel::class

    override fun get(navKey: NoticeMenuNavKey, navBackStack: NavBackStack): NoticeMenuViewModel {
        return NoticeMenuViewModel(navBackStack, baseViewModelDependencies)
    }
}
