package ru.fredboy.cavedroid.gdx.game

import kotlin.random.Random

internal class ShuffledMusicQueue(
    tracks: List<String>,
    private val random: Random = Random.Default,
) {
    private val tracks = tracks.toMutableList()
    private val queue = ArrayDeque<String>()
    private var lastPlayed: String? = null
    private var previousPlayed: String? = null

    fun next(): String? {
        if (tracks.isEmpty()) return null
        if (queue.isEmpty()) {
            val shuffled = tracks.shuffled(random).toMutableList()
            if (shuffled.size > 1 && shuffled.first() == lastPlayed) {
                val replacement = random.nextInt(1, shuffled.size)
                shuffled[0] = shuffled[replacement].also { shuffled[replacement] = shuffled[0] }
            }
            queue.addAll(shuffled)
        }
        previousPlayed = lastPlayed
        return queue.removeFirst().also { lastPlayed = it }
    }

    fun exclude(track: String) {
        tracks.remove(track)
        queue.removeAll { it == track }
        if (lastPlayed == track) lastPlayed = previousPlayed
    }
}
