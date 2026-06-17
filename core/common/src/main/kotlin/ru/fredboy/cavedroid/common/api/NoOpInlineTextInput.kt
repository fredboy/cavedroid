package ru.fredboy.cavedroid.common.api

object NoOpInlineTextInput : InlineTextInput {
    override val isSupported: Boolean = false
    override fun trigger(
        initialText: String,
        initialCursor: Int,
        buttonText: String,
        onValueChanged: (String, Int) -> Unit,
    ) = Unit
}
