package ru.fredboy.cavedroid.gdx

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.gdx.di.ApplicationComponent

class AssetPackReloadTest {
    private val current = mockk<ApplicationComponent>(relaxed = true)
    private val candidate = mockk<ApplicationComponent>(relaxed = true)
    private var selection = listOf("previous")

    init {
        every { current.assetPackRepository.pendingIds } answers { selection }
        every { current.assetPackRepository.saveSelection(any()) } answers { selection = firstArg() }
    }

    @Test
    fun `candidate validates definitions assets and skin before activation without disposing current resources`() {
        var active = current

        reloadAssetPacks(current, listOf("new"), { candidate }) { active = it }

        assertSame(candidate, active)
        assertEquals(listOf("new"), selection)
        verifyOrder {
            current.assetPackRepository.saveSelection(listOf("new"))
            candidate.packDefinitionValidator.validate()
            candidate.backgroundMusicAssets.validate()
            candidate.initializeAssets.invoke()
            candidate.itemsRepository.getAllItems()
            candidate.mobParamsRepository.getAllParams()
            candidate.menuSkin
        }
        verify(exactly = 0) { current.disposeAssets.invoke() }
        verify(exactly = 0) { candidate.disposeAssets.invoke() }
    }

    @Test
    fun `invalid candidate restores saved selection and releases partially loaded resources`() {
        val failure = IllegalArgumentException("Invalid mob")
        every { candidate.mobParamsRepository.getAllParams() } throws failure
        var activated = false

        val thrown = assertThrows(IllegalArgumentException::class.java) {
            reloadAssetPacks(current, emptyList(), { candidate }) { activated = true }
        }

        assertSame(failure, thrown)
        assertFalse(activated)
        assertEquals(listOf("previous"), selection)
        verify { candidate.disposeAssets.invoke() }
        verify { candidate.assetTextureLoader.disposeAll() }
        verify(exactly = 0) { current.disposeAssets.invoke() }
    }

    @Test
    fun `failed world activation releases candidate skin and rolls back selection`() {
        assertThrows(IllegalStateException::class.java) {
            reloadAssetPacks(current, listOf("new"), { candidate }) { error("Cannot load world") }
        }

        assertEquals(listOf("previous"), selection)
        verify { candidate.menuSkin.dispose() }
        verify { candidate.disposeAssets.invoke() }
        verify { candidate.assetTextureLoader.disposeAll() }
    }

    @Test
    fun `component creation failure rolls back selection`() {
        assertThrows(IllegalStateException::class.java) {
            reloadAssetPacks(current, emptyList(), { error("Cannot create component") }) { }
        }

        assertEquals(listOf("previous"), selection)
        verify(exactly = 0) { current.disposeAssets.invoke() }
    }

    @Test
    fun `invalid BGM restores selection before candidate activation or current resource disposal`() {
        val failure = IllegalArgumentException("bgm/index.json: missing music file")
        every { candidate.backgroundMusicAssets.validate() } throws failure
        var activated = false
        val thrown = assertThrows(IllegalArgumentException::class.java) {
            reloadAssetPacks(current, listOf("bad-music"), { candidate }) { activated = true }
        }
        assertSame(failure, thrown)
        assertFalse(activated)
        assertEquals(listOf("previous"), selection)
        verify(exactly = 0) { candidate.initializeAssets.invoke() }
        verify(exactly = 0) { current.disposeAssets.invoke() }
        verify { candidate.disposeAssets.invoke() }
        verify { candidate.assetTextureLoader.disposeAll() }
    }
}
