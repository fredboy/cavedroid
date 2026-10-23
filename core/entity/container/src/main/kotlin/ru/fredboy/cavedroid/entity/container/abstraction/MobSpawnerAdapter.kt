package ru.fredboy.cavedroid.entity.container.abstraction

import ru.fredboy.cavedroid.entity.container.model.ContainerCoordinates

interface MobSpawnerAdapter {
    fun isPlayerNearby(coordinates: ContainerCoordinates): Boolean

    /** Returns false for invalid mob keys, crowded areas or obstructed spawn positions. */
    fun trySpawn(coordinates: ContainerCoordinates, mobKey: String): Boolean
}
