package ru.fredboy.cavedroid.gdx.menu.v2.view.singleplayer

sealed interface SinglePlayerMenuState {

    data object LoadingList : SinglePlayerMenuState

    data object LoadingWorld : SinglePlayerMenuState

    data object LoadingFailed : SinglePlayerMenuState

    data class ShowList(
        val saves: List<SaveInfoVo>,
        val showMessageIfEmpty: Boolean = true,
    ) : SinglePlayerMenuState
}
