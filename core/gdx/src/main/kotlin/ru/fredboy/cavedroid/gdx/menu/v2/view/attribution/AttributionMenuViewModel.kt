package ru.fredboy.cavedroid.gdx.menu.v2.view.attribution

import com.badlogic.gdx.Gdx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies

class AttributionMenuViewModel(
    private val navBackStack: NavBackStack,
    baseViewModelDependencies: BaseViewModelDependencies,
) : BaseViewModel(baseViewModelDependencies) {

    private val _attributionsFlow = MutableSharedFlow<String>()

    private val attributionsFlow = _attributionsFlow
        .onStart {
            val attributions = withContext(Dispatchers.IO) {
                getAllAttributions()
            }
            emit(attributions)
        }

    val stateFlow: StateFlow<String> = attributionsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500L),
            initialValue = "",
        )

    fun getAllAttributions(): String {
        val indexFile = Gdx.files.internal("attribution_index.txt")
        if (!indexFile.exists()) return "No attributions found."

        val paths = indexFile.readString().lines().filter { it.isNotBlank() }
        return buildString {
            for (path in paths) {
                val file = Gdx.files.internal(path)
                if (file.exists()) {
                    append("${file.path()}\n\n")
                    append(file.readString().trim('\n', ' '))
                    append("\n\n================\n\n")
                }
            }
        }
    }

    fun onBackClick() {
        navBackStack.pop()
    }
}
