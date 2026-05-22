package ru.fredboy.cavedroid.gameplay.controls.input.action.keys

import co.touchlab.kermit.Logger

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
        override val pointer: Int,
    ) : Touch {
        override val touchUp: Boolean
            get() {
                val e = IllegalAccessException("not applicable for mouse dragged action")
                logger.w(e) { "Checking touch up on Dragged" }
                return false
            }
    }

    data class Left(
        override val touchUp: Boolean,
    ) : MouseInputActionKey

    data class Right(
        override val touchUp: Boolean,
    ) : MouseInputActionKey

    data class Middle(
        override val touchUp: Boolean,
    ) : MouseInputActionKey

    data class Screen(
        override val touchUp: Boolean,
        override val pointer: Int,
    ) : Touch

    data class Scroll(
        val amountX: Float,
        val amountY: Float,
    ) : MouseInputActionKey {
        override val touchUp: Boolean
            get() {
                val e = IllegalAccessException("not applicable for mouse scroll action")
                logger.w(e) { "Checking touch up on Scrolled" }
                return false
            }
    }

    companion object {
        private val logger = Logger.withTag("MouseInputActionKey")
    }
}
