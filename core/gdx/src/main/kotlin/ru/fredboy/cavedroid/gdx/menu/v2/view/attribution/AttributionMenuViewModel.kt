package ru.fredboy.cavedroid.gdx.menu.v2.view.attribution

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
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

    private fun getAllAttributions(): String {
        val root = Gdx.files.internal(".")
        val attributions = StringBuilder()

        fun processDirectory(dir: FileHandle) {
            for (file in dir.list()) {
                if (file.isDirectory) {
                    processDirectory(file)
                } else if (file.name().equals("attribution.txt", ignoreCase = true)) {
                    attributions.append("${file.path()}\n\n")
                    attributions.append(file.readString().trim('\n', ' '))
                    attributions.append("\n\n================\n\n")
                }
            }
        }
        processDirectory(root)

        return attributions.toString()
    }

    fun onBackClick() {
        navBackStack.pop()
    }
}
