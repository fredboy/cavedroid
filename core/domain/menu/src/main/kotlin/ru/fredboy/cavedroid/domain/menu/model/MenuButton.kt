package ru.fredboy.cavedroid.domain.menu.model

sealed class MenuButton {

    abstract val label: String
    abstract val isVisible: Boolean
    abstract val actionKey: String
    abstract val isEnabled: Boolean

    data class Simple(
        override val label: String,
        override val isVisible: Boolean,
        override val actionKey: String,
        override val isEnabled: Boolean,
    ) : MenuButton()

    sealed class Option : MenuButton() {
        abstract val optionKeys: List<String>
    }

    data class BooleanOption(
        override val label: String,
        override val isVisible: Boolean,
        override val actionKey: String,
        override val isEnabled: Boolean,
        override val optionKeys: List<String>,
    ) : Option()

    data class NumericalOption(
        override val label: String,
        override val isVisible: Boolean,
        override val actionKey: String,
        override val isEnabled: Boolean,
        override val optionKeys: List<String>,
    ) : Option()
}
