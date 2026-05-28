package ru.fredboy.cavedroid.common.utils

interface PriorityMapValue {
    val priority: Int
}

class PriorityMap<K, V : PriorityMapValue>(
    private val delegate: MutableMap<K, V> = LinkedHashMap(),
    private val comparator: Comparator<Int> = Comparator { a, b -> a.compareTo(b) },
) : MutableMap<K, V> by delegate {

    override fun put(key: K, value: V): V? {
        val current = delegate[key]

        if (current != null && comparator.compare(current.priority, value.priority) < 0) {
            return null
        }

        delegate[key] = value

        return current
    }
}
