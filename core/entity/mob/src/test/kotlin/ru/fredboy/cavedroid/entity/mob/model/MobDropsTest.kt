package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.graphics.g2d.Sprite
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.drop.DropAmount
import ru.fredboy.cavedroid.domain.items.model.drop.DropInfo
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.CommonItemParams
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase

class MobDropsTest {

    private val cooked = item(food = true)

    @Test
    fun `burning mob drops the configured smelting product and preserves the amount`() {
        val raw = item(food = true, product = "cooked_food")
        val drop = drops(raw).single()

        assertSame(cooked, drop.item)
        assertEquals(3, drop.amount)
    }

    @Test
    fun `mob that is not burning drops raw food`() {
        val raw = item(food = true, product = "cooked_food")

        assertSame(raw, drops(raw, burning = false).single().item)
    }

    @Test
    fun `burning mob leaves food without a smelting product unchanged`() {
        val food = item(food = true)

        assertSame(food, drops(food).single().item)
    }

    @Test
    fun `burning mob does not smelt nonfood drops`() {
        val nonfood = item(food = false, product = "cooked_food")

        assertSame(nonfood, drops(nonfood).single().item)
    }

    @Test
    fun `cooking preserves random range quantities`() {
        val raw = item(food = true, product = "cooked_food")
        val drop = drops(raw, amount = DropAmount.RandomRange(1..2, 1f)).single()

        assertSame(cooked, drop.item)
        assertTrue(drop.amount in 1..2)
    }

    @Test
    fun `cooking does not create drops that failed their chance roll`() {
        val raw = item(food = true, product = "cooked_food")

        assertTrue(drops(raw, amount = DropAmount.RandomChance(0f, 1)).isEmpty())
        assertTrue(drops(raw, amount = DropAmount.RandomRange(1..2, 0f)).isEmpty())
    }

    private fun item(food: Boolean, product: String? = null): Item {
        val params = mockk<CommonItemParams> {
            every { smeltProductKey } returns product
        }
        return if (food) Item.Food(params, Sprite(), heal = 2, saturation = 1.2f) else Item.Normal(params, Sprite())
    }

    private fun drops(
        item: Item,
        burning: Boolean = true,
        amount: DropAmount = DropAmount.ExactAmount(3),
    ): List<InventoryItem> {
        val repository = mockk<ItemsRepository> {
            every { getItemByKey("raw_food") } returns item
            every { getItemByKey("cooked_food") } returns cooked
        }
        val params = mockk<MobParams>(relaxed = true) {
            every { behaviorType } returns MobBehaviorType.PASSIVE
            every { dropInfo } returns listOf(DropInfo("raw_food", false, amount))
        }
        val mob = WalkingMob(params, Direction.RIGHT)
        if (burning) mob.ignite(5f)
        mob.kill()
        return mob.getDropItems(GetItemByKeyUseCase(repository))
    }
}
