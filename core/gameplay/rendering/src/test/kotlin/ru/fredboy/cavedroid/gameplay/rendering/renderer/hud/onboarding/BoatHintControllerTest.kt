package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.onboarding

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository

class BoatHintControllerTest {
    private var completed = false
    private val context = mockk<ApplicationContextRepository>(relaxed = true) {
        every { isTouch() } returns true
        every { isBoatHintShown() } answers { completed }
        every { setBoatHintShown(any()) } answers { completed = firstArg() }
    }
    private val hints = BoatHintController(context)

    @Test
    fun `no boat hint is shown before placement or mounting`() {
        hints.update(60f)
        assertNull(hints.state)
    }

    @Test
    fun `water placement shows mount tip without a timeout`() {
        hints.notifyPlacedAfloat("boat")
        hints.update(600f)
        assertSame(OnboardingStep.MountBoat, hints.state?.step)
        assertEquals(true, hints.state?.awaitingInput)
        assertFalse(completed)
    }

    @Test
    fun `mounting immediately replaces the mount tip with the dismount tip`() {
        hints.notifyPlacedAfloat("boat")
        hints.notifyMounted("boat")
        hints.update(600f)
        assertSame(OnboardingStep.DismountBoat, hints.state?.step)
        assertEquals(true, hints.state?.awaitingInput)
        assertFalse(completed)
    }

    @Test
    fun `restoring a passenger also teaches dismounting`() {
        hints.notifyMounted("saved-boat")
        assertSame(OnboardingStep.DismountBoat, hints.state?.step)
    }

    @Test
    fun `successful dismount fades the tip and remembers completion`() {
        hints.notifyMounted("boat")
        hints.notifyDetached(dismounted = true)
        assertTrue(completed)
        assertEquals(false, hints.state?.awaitingInput)
        hints.update(0.75f)
        assertEquals(0.5f, hints.state?.cooldownProgress)
        hints.update(0.75f)
        assertNull(hints.state)
        verify(exactly = 1) { context.setBoatHintShown(true) }
    }

    @Test
    fun `completed hints stay hidden in the next session`() {
        hints.notifyMounted("boat")
        hints.notifyDetached(dismounted = true)
        val nextSession = BoatHintController(context)
        nextSession.notifyPlacedAfloat("another-boat")
        nextSession.notifyMounted("another-boat")
        assertNull(nextSession.state)
    }

    @Test
    fun `resetting the preference enables hints in the next session`() {
        completed = true
        context.setBoatHintShown(false)
        val nextSession = BoatHintController(context)
        nextSession.notifyPlacedAfloat("boat")
        assertSame(OnboardingStep.MountBoat, nextSession.state?.step)
    }

    @Test
    fun `death or forced detachment cancels the hint without completing it`() {
        hints.notifyMounted("boat")
        hints.notifyDetached(dismounted = false)
        assertNull(hints.state)
        assertFalse(completed)
        hints.notifyMounted("boat")
        assertSame(OnboardingStep.DismountBoat, hints.state?.step)
    }

    @Test
    fun `removing the pending boat cancels the mounting hint`() {
        hints.notifyPlacedAfloat("boat")
        hints.notifyRemoved("other-boat")
        assertSame(OnboardingStep.MountBoat, hints.state?.step)
        hints.notifyRemoved("boat")
        assertNull(hints.state)
        assertFalse(completed)
    }

    @Test
    fun `placing another boat while mounted does not replace the dismount tip`() {
        hints.notifyMounted("boat")
        hints.notifyPlacedAfloat("other-boat")
        hints.notifyRemoved("other-boat")
        assertSame(OnboardingStep.DismountBoat, hints.state?.step)
    }

    @Test
    fun `desktop controls never show boat hints`() {
        every { context.isTouch() } returns false
        hints.notifyPlacedAfloat("boat")
        hints.notifyMounted("boat")
        hints.notifyDetached(dismounted = true)
        assertNull(hints.state)
        assertFalse(completed)
    }
}
