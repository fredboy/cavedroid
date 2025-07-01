package ru.fredboy.cavedroid.common.utils

fun <T> MutableCollection<T>.retrieveFirst(predicate: (T) -> Boolean): T? {
    val iterator = this.iterator()
    while (iterator.hasNext()) {
        val element = iterator.next()
        if (predicate.invoke(element)) {
            iterator.remove()
            return element
        }
    }

    return null
}

fun <T> MutableCollection<T>.removeFirst(predicate: (T) -> Boolean): Boolean = retrieveFirst(predicate) != null
