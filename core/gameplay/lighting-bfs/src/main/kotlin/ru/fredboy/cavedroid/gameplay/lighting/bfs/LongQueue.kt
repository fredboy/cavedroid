package ru.fredboy.cavedroid.gameplay.lighting.bfs

/** Reusable primitive FIFO: lighting propagation must not allocate a boxed Long for every cell. */
internal class LongQueue {
    private var values = LongArray(256)
    private var head = 0
    private var size = 0

    fun isNotEmpty() = size != 0

    fun addLast(value: Long) {
        if (size == values.size) {
            val larger = LongArray(values.size * 2)
            for (i in 0 until size) larger[i] = values[(head + i) % values.size]
            values = larger
            head = 0
        }
        values[(head + size) % values.size] = value
        size++
    }

    fun removeFirst(): Long {
        check(size > 0)
        val value = values[head]
        head = (head + 1) % values.size
        size--
        if (size == 0) head = 0
        return value
    }
}
