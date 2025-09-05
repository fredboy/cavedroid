package ru.fredboy.cavedroid.gdx.menu.v2.view.notice

import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModelProvider
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class NoticeMenuViewModelProvider @Inject constructor() : ViewModelProvider<NoticeMenuNavKey, NoticeMenuViewModel> {

    override val viewModelClass: KClass<NoticeMenuViewModel>
        get() = NoticeMenuViewModel::class

    override fun get(navKey: NoticeMenuNavKey, navBackStack: NavBackStack): NoticeMenuViewModel {
        return NoticeMenuViewModel(navBackStack)
    }
}
