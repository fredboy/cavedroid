package ru.deadsoftware.cavedroid.game.input.action.keys

sealed interface MouseInputActionKey {

    val touchUp: Boolean

    data object None : MouseInputActionKey {
        override val touchUp: Boolean
            get() = throw IllegalAccessException("not applicable for mouse move action")
    }

    data object Dragged : MouseInputActionKey {
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

    data class Touch(
        override val touchUp: Boolean
    ) : MouseInputActionKey

    data class Scroll(
        val amountX: Float,
        val amountY: Float
    ) : MouseInputActionKey {
        override val touchUp: Boolean
            get() = throw IllegalAccessException("not applicable for mouse scroll action")
    }

}