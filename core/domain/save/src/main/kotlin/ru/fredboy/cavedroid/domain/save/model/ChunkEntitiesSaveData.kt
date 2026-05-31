package ru.fredboy.cavedroid.domain.save.model

import ru.fredboy.cavedroid.entity.container.model.Container
import ru.fredboy.cavedroid.entity.container.model.ContainerCoordinates
import ru.fredboy.cavedroid.entity.drop.model.Drop
import ru.fredboy.cavedroid.entity.mob.model.Mob

/**
 * Reconstructed entities that belong to a single infinite-world chunk. Mobs and drops are already
 * spawned (physics bodies created); containers are ready to be registered by coordinate.
 */
class ChunkEntitiesSaveData(
    val mobs: List<Mob>,
    val drops: List<Drop>,
    val containers: Map<ContainerCoordinates, Container>,
)
