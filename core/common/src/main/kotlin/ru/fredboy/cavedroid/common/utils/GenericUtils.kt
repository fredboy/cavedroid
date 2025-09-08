package ru.fredboy.cavedroid.common.utils

import java.util.Locale

inline fun <reified T> Boolean.ifTrue(func: () -> T): T? {
    return if (this) {
        func()
    } else {
        null
    }
}

inline fun <reified T> Boolean.ifFalse(func: () -> T): T? = (!this).ifTrue(func)

fun Boolean.toToggleStateString(): String = if (this) "ON" else "OFF"

fun Boolean.takeIfTrue(): Boolean? = takeIf { it }

fun String.startWithCapital(locale: Locale) = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(locale) else it.toString()
}
