package ru.fredboy.cavedroid.entity.vehicle

import com.badlogic.gdx.physics.box2d.Body
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VehicleTest {
    private val boat = Vehicle("boat", "oak_boat", mockk<Body>())

    @Test
    fun `ten damage destroys a boat and further hits cannot underflow`() {
        boat.damage(3)
        assertEquals(7, boat.health)
        boat.damage(Int.MAX_VALUE)
        boat.damage(1)
        assertEquals(0, boat.health)
    }

    @Test
    fun `negative damage does not heal`() {
        boat.damage(3)
        boat.damage(-10)
        assertEquals(7, boat.health)
    }
}
