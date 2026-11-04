package ru.fredboy.cavedroid.gdx.menu.v2.view.assetpacks

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.api.AssetPackPlatform
import ru.fredboy.cavedroid.common.coroutines.AppDispatchers
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.domain.assets.pack.AssetPackRepository
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies

class AssetPacksViewModelTest {
    private val previousApp = Gdx.app
    private val posted = ArrayDeque<Runnable>()
    private var callback: ((Result<FileHandle?>) -> Unit)? = null
    private val platform = object : AssetPackPlatform {
        override val isSupported = true
        override fun pickArchive(onResult: (Result<FileHandle?>) -> Unit) {
            callback = onResult
        }
        override fun extractArchive(archive: FileHandle, destination: FileHandle): String = error("Not used")
    }
    private val repository = mockk<AssetPackRepository> {
        every { installed() } returns emptyList()
        every { pendingIds } returns listOf("selected")
        every { runningIds } returns emptyList()
        every { startupError } returns null
    }
    private val applicationController = mockk<ApplicationController>(relaxed = true)
    private lateinit var viewModel: AssetPacksViewModel

    @BeforeEach
    fun setup() {
        Gdx.app = mockk<Application> {
            every { postRunnable(any()) } answers { posted.addLast(firstArg()) }
        }
        val dependencies = mockk<BaseViewModelDependencies> {
            every { dispatchers } returns AppDispatchers(Dispatchers.Unconfined, Dispatchers.Unconfined, Dispatchers.Unconfined)
        }
        viewModel = AssetPacksViewModel(repository, platform, NavBackStack(AssetPacksNavKey), applicationController, dependencies)
    }

    @AfterEach
    fun cleanup() {
        viewModel.dispose()
        Gdx.app = previousApp
    }

    @Test
    fun `cancelling picker clears busy state without importing or changing selection`() {
        viewModel.importPack()
        assertTrue(viewModel.state.value.busy)
        callback!!(Result.success(null))
        assertTrue(viewModel.state.value.busy)
        posted.removeFirst().run()
        assertFalse(viewModel.state.value.busy)
        assertNull(viewModel.state.value.message)
        assertEquals(listOf("selected"), viewModel.state.value.selected)
        verify(exactly = 0) { repository.importArchive(any()) }
        viewModel.importPack()
        assertTrue(viewModel.state.value.busy)
    }

    @Test
    fun `picker failure reports error and clears busy state`() {
        viewModel.importPack()
        callback!!(Result.failure(IllegalStateException("Picker failed")))
        posted.removeFirst().run()
        assertFalse(viewModel.state.value.busy)
        assertEquals("Picker failed", viewModel.state.value.message)
    }

    @Test
    fun `cancellation arriving after menu disposal is harmless`() {
        viewModel.importPack()
        viewModel.dispose()
        callback!!(Result.success(null))
        posted.removeFirst().run()
        verify(exactly = 0) { repository.importArchive(any()) }
    }

    @Test
    fun `done applies changed selection after the current frame and ignores repeated clicks`() {
        viewModel.done()
        viewModel.done()
        assertTrue(viewModel.state.value.busy)
        verify(exactly = 0) { applicationController.applyAssetPacks(any()) }

        posted.removeFirst().run()

        verify(exactly = 1) { applicationController.applyAssetPacks(listOf("selected")) }
        assertTrue(posted.isEmpty())
    }

    @Test
    fun `apply failure leaves menu editable and displays the error`() {
        every { applicationController.applyAssetPacks(any()) } throws IllegalArgumentException("Invalid skin")
        viewModel.done()

        posted.removeFirst().run()

        assertFalse(viewModel.state.value.busy)
        assertEquals("Invalid skin", viewModel.state.value.message)
        assertEquals(listOf("selected"), viewModel.state.value.selected)
    }

    @Test
    fun `done with unchanged selection avoids rebuilding assets`() {
        every { repository.runningIds } returns listOf("selected")
        every { repository.saveSelection(any()) } returns Unit

        viewModel.done()

        assertTrue(posted.isEmpty())
        verify { repository.saveSelection(listOf("selected")) }
        verify(exactly = 0) { applicationController.applyAssetPacks(any()) }
    }

    @Test
    fun `disposed menu cannot apply a queued selection`() {
        viewModel.done()
        viewModel.dispose()

        posted.removeFirst().run()

        verify(exactly = 0) { applicationController.applyAssetPacks(any()) }
    }
}
