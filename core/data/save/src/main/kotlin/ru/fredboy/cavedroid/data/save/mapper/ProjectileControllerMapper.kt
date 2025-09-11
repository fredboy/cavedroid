package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.projectile.abstraction.ProjectileWorldAdapter
import ru.fredboy.cavedroid.game.controller.projectile.ProjectileController
import javax.inject.Inject

@Reusable
class ProjectileControllerMapper @Inject constructor(
    private val projectileMapper: ProjectileMapper,
) {

    fun mapSaveData(projectileController: ProjectileController): SaveDataDto.ProjectileControllerSaveDataDto {
        return SaveDataDto.ProjectileControllerSaveDataDto(
            version = SAVE_DATA_VERSION,
            projectiles = projectileController.projectiles.map { projectileMapper.mapSaveData(it) },
        )
    }

    fun mapProjectileController(
        saveDataDto: SaveDataDto.ProjectileControllerSaveDataDto,
        projectileWorldAdapter: ProjectileWorldAdapter,
        dropQueue: DropQueue,
    ): ProjectileController {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return ProjectileController(
            initialProjectiles = saveDataDto.projectiles.map {
                projectileMapper.mapProjectile(it, projectileWorldAdapter)
            },
            projectileWorldAdapter = projectileWorldAdapter,
            dropQueue = dropQueue,
        )
    }

    companion object {
        private const val SAVE_DATA_VERSION = 1
    }
}
