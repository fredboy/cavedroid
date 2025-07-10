package ru.fredboy.cavedroid.common.utils

inline fun <reified T> Boolean.ifTrue(func: () -> T): T? {
    return if (this) {
        func()
    } else {
        null
    }
}

inline fun <reified T> Boolean.ifFalse(func: () -> T): T? = (!this).ifTrue(func)
