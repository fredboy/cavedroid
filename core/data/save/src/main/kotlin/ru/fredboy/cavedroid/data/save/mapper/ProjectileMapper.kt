package ru.fredboy.cavedroid.data.save.mapper

import com.badlogic.gdx.math.Vector2
import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.projectile.abstraction.ProjectileWorldAdapter
import ru.fredboy.cavedroid.entity.projectile.model.Projectile
import javax.inject.Inject

@Reusable
class ProjectileMapper @Inject constructor(
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
) {

    fun mapSaveData(projectile: Projectile): SaveDataDto.ProjectileSaveDataDto {
        return SaveDataDto.ProjectileSaveDataDto(
            version = SAVE_DATA_VERSION,
            x = projectile.position.x,
            y = projectile.position.y,
            width = projectile.width,
            height = projectile.height,
            velocityX = projectile.velocity.x,
            velocityY = projectile.velocity.y,
            itemKey = projectile.item.params.key,
            damage = projectile.damage,
            dropOnGround = projectile.dropOnGround,
        )
    }

    fun mapProjectile(
        saveDataDto: SaveDataDto.ProjectileSaveDataDto,
        projectileWorldAdapter: ProjectileWorldAdapter,
    ): Projectile {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return Projectile(
            item = getItemByKeyUseCase[saveDataDto.itemKey],
            damage = saveDataDto.damage,
            width = saveDataDto.width,
            height = saveDataDto.height,
            dropOnGround = saveDataDto.dropOnGround,
        ).apply {
            spawn(
                x = saveDataDto.x,
                y = saveDataDto.y,
                velocity = Vector2.Zero,
                world = projectileWorldAdapter.getBox2dWorld(),
            )
            velocity.y = saveDataDto.velocityY
            velocity.x = saveDataDto.velocityX
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 1
    }
}
