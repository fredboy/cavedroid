package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.drop.DropAmount
import ru.fredboy.cavedroid.domain.items.model.drop.DropInfo
import ru.fredboy.cavedroid.domain.items.model.item.CommonItemParams
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase

class SheepMobTest {

    private val repository = mockk<ItemsRepository>()
    private val itemByKey = GetItemByKeyUseCase(repository)

    init {
        every { repository.getItemByKey(any()) } answers {
            val itemKey = firstArg<String>()
            val params = mockk<CommonItemParams> {
                every { key } returns itemKey
                every { smeltProductKey } returns "mutton_cooked".takeIf { itemKey == "mutton_raw" }
            }
            if (itemKey == "mutton_raw") {
                Item.Food(params, Sprite(), heal = 2, saturation = 1.2f)
            } else {
                Item.Normal(params, Sprite())
            }
        }
    }

    @Test
    fun `sheep drops raw mutton and wool matching its color`() {
        val sheep = sheep()

        assertEquals(mapOf("mutton_raw" to 2, "wool_colored_red" to 1), drops(sheep))
    }

    @Test
    fun `sheared sheep still drops mutton without wool`() {
        val sheep = sheep().apply { hasFur = false }

        assertEquals(mapOf("mutton_raw" to 2), drops(sheep))
    }

    @Test
    fun `burning sheep drops cooked mutton and keeps its colored wool`() {
        val sheep = sheep().apply { ignite(5f) }

        assertEquals(mapOf("mutton_cooked" to 2, "wool_colored_red" to 1), drops(sheep))
    }

    @Test
    fun `burning sheared sheep drops cooked mutton without wool`() {
        val sheep = sheep().apply {
            hasFur = false
            ignite(5f)
        }

        assertEquals(mapOf("mutton_cooked" to 2), drops(sheep))
    }

    @Test
    fun `extinguished sheep drops raw mutton`() {
        val sheep = sheep().apply {
            ignite(5f)
            extinguish()
        }

        assertEquals(mapOf("mutton_raw" to 2, "wool_colored_red" to 1), drops(sheep))
    }

    @Test
    fun `repeated kill preserves cooked drops after death extinguishes the fire`() {
        val sheep = sheep().apply {
            ignite(5f)
            kill()
        }

        assertEquals(mapOf("mutton_cooked" to 2, "wool_colored_red" to 1), drops(sheep))
    }

    private fun sheep(): SheepMob {
        val params = mockk<MobParams>(relaxed = true) {
            every { behaviorType } returns MobBehaviorType.SHEEP
            every { dropInfo } returns listOf(
                DropInfo("mutton_raw", false, DropAmount.ExactAmount(2)),
                DropInfo("wool_colored_white", false, DropAmount.ExactAmount(1)),
            )
        }
        return SheepMob(params, Direction.RIGHT).apply {
            woolToColor = "wool_colored_red" to Color.RED
        }
    }

    private fun drops(sheep: SheepMob): Map<String, Int> {
        sheep.kill()
        return sheep.getDropItems(itemByKey).associate { it.item.params.key to it.amount }
    }
}
