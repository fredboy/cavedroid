package ru.fredboy.cavedroid.common.utils

/**
 * Removes the first element matching the predicate and removes it from collection
 *
 * @return a removed element or null if none matched
 */
fun <T> MutableCollection<T>.retrieveFirst(predicate: (T) -> Boolean = { true }): T? {
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

/**
 * Removes the first element matching the predicate from collection
 *
 * @return if any was removed
 */
fun <T> MutableCollection<T>.removeFirst(predicate: (T) -> Boolean = { true }): Boolean {
    return retrieveFirst(predicate) != null
}
