package ru.fredboy.cavedroid.gdx.menu.v2.view.newgame

import ru.fredboy.cavedroid.common.api.InlineTextInput
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.common.mvvm.ViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.di.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class NewGameMenuViewModelProvider @Inject constructor(
    private val inlineTextInput: InlineTextInput,
    private val baseViewModelDependencies: BaseViewModelDependencies,
) : ViewModelProvider<NewGameMenuNavKey, NewGameMenuViewModel> {

    override val viewModelClass: KClass<NewGameMenuViewModel>
        get() = NewGameMenuViewModel::class

    override fun get(navKey: NewGameMenuNavKey, navBackStack: NavBackStack): NewGameMenuViewModel {
        return NewGameMenuViewModel(
            navBackStack = navBackStack,
            inlineTextInput = inlineTextInput,
            baseViewModelDependencies = baseViewModelDependencies,
        )
    }
}
