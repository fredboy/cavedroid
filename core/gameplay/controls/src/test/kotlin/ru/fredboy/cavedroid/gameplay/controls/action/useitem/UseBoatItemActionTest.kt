package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.entity.vehicle.BoatType
import ru.fredboy.cavedroid.game.controller.vehicle.VehicleController

class UseBoatItemActionTest {
    private val vehicles = mockk<VehicleController>()
    private val action = UseBoatItemAction(vehicles)

    @ParameterizedTest
    @EnumSource(BoatType::class)
    fun `using a boat item places its wood type`(type: BoatType) {
        val item = mockk<Item.Usable> { every { params.key } returns type.itemKey }
        every { vehicles.place(33.5f, 20.75f, type.itemKey) } returns true
        assertTrue(action.perform(item, 33, 20))
        verify(exactly = 1) { vehicles.place(33.5f, 20.75f, type.itemKey) }
    }

    @Test
    fun `failed placement is reported to the caller`() {
        val item = mockk<Item.Usable> { every { params.key } returns "birch_boat" }
        every { vehicles.place(any(), any(), any()) } returns false
        assertFalse(action.perform(item, 33, 20))
    }
}
