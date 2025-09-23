package ru.fredboy.cavedroid.common.utils

import java.util.Locale

@JvmName("ifTrueExtension")
inline fun <reified T> Boolean.ifTrue(func: () -> T): T? {
    return if (this) {
        func()
    } else {
        null
    }
}

@JvmName("ifFalseExtension")
inline fun <reified T> Boolean.ifFalse(func: () -> T): T? = (!this).ifTrue(func)

inline fun <reified T> ifTrue(bool: Boolean, func: () -> T): T? = bool.ifTrue(func)

inline fun <reified T> ifFalse(bool: Boolean, func: () -> T): T? = bool.ifFalse(func)

fun Boolean.toToggleStateString(): String = if (this) "ON" else "OFF"

fun Boolean.takeIfTrue(): Boolean? = takeIf { it }

fun String.startWithCapital(locale: Locale) = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(locale) else it.toString()
}
