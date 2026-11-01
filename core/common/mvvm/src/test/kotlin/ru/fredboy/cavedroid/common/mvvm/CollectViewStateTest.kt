package ru.fredboy.cavedroid.common.mvvm

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CollectViewStateTest {
    private var previousApp: Application? = null

    @BeforeEach
    fun setUp() {
        previousApp = Gdx.app
        Gdx.app = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        Gdx.app = previousApp
    }

    private fun pane(name: String = "content", contentSize: Float = 1000f): ScrollPane {
        val content = Actor().apply { setSize(contentSize, contentSize) }
        return ScrollPane(content, ScrollPane.ScrollPaneStyle()).apply {
            this.name = name
            setSize(100f, 100f)
        }
    }

    @Test
    fun `state rebuild replaces actors and restores both scroll axes and focus immediately`() = runTest {
        val root = Group()
        val stage = mockk<Stage>(relaxed = true)
        every { stage.root } returns root
        every { stage.clear() } answers { root.clearChildren() }
        var focused: Actor? = null
        every { stage.scrollFocus } answers { focused }
        every { stage.setScrollFocus(any()) } answers {
            focused = firstArg()
            true
        }
        lateinit var oldPane: ScrollPane
        val states = flow {
            emit(1)
            oldPane = root.children.first() as ScrollPane
            oldPane.scrollX = 120f
            oldPane.scrollY = 350f
            focused = oldPane
            emit(2)
        }

        stage.collectViewState(states) {
            root.addActor(pane())
        }

        assertEquals(1, root.children.size)
        val restored = root.children.first() as ScrollPane
        assertEquals(120f, restored.scrollX)
        assertEquals(350f, restored.scrollY)
        assertEquals(120f, restored.visualScrollX)
        assertEquals(350f, restored.visualScrollY)
        assertSame(restored, focused)
    }

    @Test
    fun `offset survives a temporary state without panes and clamps to shorter content`() {
        val state = ViewScrollState()
        val original = pane().apply {
            validate()
            scrollY = 800f
        }
        state.capture(original)
        state.capture(Group())
        state.restore(Group())
        val restored = pane(contentSize = 300f)

        state.restore(restored)

        assertEquals(200f, restored.scrollY)
        assertEquals(200f, restored.visualScrollY)
    }

    @Test
    fun `pane identity survives layout changes and does not leak into another view`() {
        val state = ViewScrollState()
        state.capture(
            pane("first").apply {
                validate()
                scrollY = 230f
            },
        )
        state.capture(
            pane("second").apply {
                validate()
                scrollY = 470f
            },
        )
        val root = Group()
        val second = pane("second")
        val first = pane("first")
        root.addActor(second)
        root.addActor(Group().apply { addActor(first) })

        state.restore(root)

        assertEquals(230f, first.scrollY)
        assertEquals(470f, second.scrollY)
        val freshView = pane("first")
        ViewScrollState().restore(freshView)
        assertEquals(0f, freshView.scrollY)
    }
}
