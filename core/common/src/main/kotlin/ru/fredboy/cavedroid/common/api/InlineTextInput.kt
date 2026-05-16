package ru.fredboy.cavedroid.common.api

interface InlineTextInput {

    val isSupported: Boolean

    fun trigger(
        initialText: String,
        initialCursor: Int,
        onValueChanged: (text: String, cursor: Int) -> Unit,
    )
}
