package ru.deadsoftware.cavedroid.game.input.action.keys

sealed interface MouseInputActionKey {

    val touchUp: Boolean

    sealed interface Touch : MouseInputActionKey {
        val pointer: Int
    }

    data object None : MouseInputActionKey {
        override val touchUp: Boolean
            get() = throw IllegalAccessException("not applicable for mouse move action")
    }

    data class Dragged(
        override val pointer: Int
    ) : Touch {
        override val touchUp: Boolean
            get() = throw IllegalAccessException("not applicable for mouse dragged action")
    }

    data class Left(
        override val touchUp: Boolean
    ) : MouseInputActionKey

    data class Right(
        override val touchUp: Boolean
    ) : MouseInputActionKey

    data class Middle(
        override val touchUp: Boolean
    ) : MouseInputActionKey

    data class Screen(
        override val touchUp: Boolean,
        override val pointer: Int,
    ) : Touch

    data class Scroll(
        val amountX: Float,
        val amountY: Float
    ) : MouseInputActionKey {
        override val touchUp: Boolean
            get() = throw IllegalAccessException("not applicable for mouse scroll action")
    }

}