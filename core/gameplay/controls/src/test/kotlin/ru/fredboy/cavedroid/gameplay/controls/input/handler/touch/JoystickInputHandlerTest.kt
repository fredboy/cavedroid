package ru.fredboy.cavedroid.gameplay.controls.input.handler.touch

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.vehicle.VehicleController
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey

class JoystickInputHandlerTest {
    private val player = mockk<Player>(relaxed = true) { every { isMounted } returns true }
    private val mobs = mockk<MobController> { every { player } returns this@JoystickInputHandlerTest.player }
    private val context = mockk<GameContextRepository> { every { getJoystick() } returns Joystick(4f) }
    private val vehicles = mockk<VehicleController>(relaxed = true)
    private val handler = JoystickInputHandler(mockk(), context, mobs, vehicles, mockk(), mockk())

    @Test
    fun `resetting active touch input does not request dismount or flight`() {
        handler.handle(MouseInputAction(10f, 10f, MouseInputActionKey.Screen(false, 0)))
        handler.reset()
        verify(exactly = 0) { vehicles.dismount() }
        verify(exactly = 0) { player.jump() }
        verify(exactly = 0) { player.isFlyMode = true }
    }
}
