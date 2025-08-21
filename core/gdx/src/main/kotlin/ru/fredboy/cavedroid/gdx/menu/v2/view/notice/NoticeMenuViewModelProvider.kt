package ru.fredboy.cavedroid.gdx.menu.v2.view.notice

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavKey
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModelProvider
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class NoticeMenuViewModelProvider @Inject constructor(
    private val applicationController: ApplicationController,
) : ViewModelProvider<NoticeMenuViewModel> {

    override val viewModelClass: KClass<NoticeMenuViewModel>
        get() = NoticeMenuViewModel::class

    override fun get(navKey: NavKey, navBackStack: NavBackStack): NoticeMenuViewModel {
        return NoticeMenuViewModel(navBackStack)
    }
}
