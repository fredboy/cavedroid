package ru.fredboy.cavedroid.gdx.menu.v2.view.notice

import com.badlogic.gdx.Gdx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModel

class NoticeMenuViewModel(
    private val navBackStack: NavBackStack,
) : ViewModel() {

    private val _noticesFlow = MutableSharedFlow<String>()

    private val noticesFlow = _noticesFlow
        .onStart {
            val notices = withContext(Dispatchers.IO) {
                Gdx.files.internal("notices.txt").readString()
            }
            emit(notices)
        }

    val stateFlow: StateFlow<NoticeMenuState> = noticesFlow
        .map { notices ->
            NoticeMenuState(
                notices = notices,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500L),
            initialValue = NoticeMenuState(""),
        )

    fun onCopyClicked(notices: String) {
        Gdx.app.clipboard.contents = notices
    }

    fun onBackClick() {
        navBackStack.pop()
    }
}
