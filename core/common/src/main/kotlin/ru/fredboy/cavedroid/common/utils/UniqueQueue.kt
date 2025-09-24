package ru.fredboy.cavedroid.common.utils

import java.util.Queue

class UniqueQueue<E>(
    private val backingSet: LinkedHashSet<E> = LinkedHashSet(),
) : Queue<E>,
    MutableSet<E> by backingSet {

    /**
     * Attempts to add en element to the queue
     *
     * @return true if was added and false if not
     */
    override fun offer(element: E): Boolean {
        return backingSet.add(element)
    }

    override fun remove(): E {
        return poll() ?: throw NoSuchElementException()
    }

    override fun poll(): E? {
        return backingSet.retrieveFirst()
    }

    override fun element(): E {
        return backingSet.first()
    }

    override fun peek(): E? {
        return backingSet.firstOrNull()
    }
}
