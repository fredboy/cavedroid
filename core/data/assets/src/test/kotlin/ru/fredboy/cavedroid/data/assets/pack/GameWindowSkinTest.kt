package ru.fredboy.cavedroid.data.assets.pack

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.utils.Array
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.assets.pack.AssetResolver
import ru.fredboy.cavedroid.domain.assets.pack.AssetTextureLoader
import java.io.File
import java.nio.ByteBuffer

class GameWindowSkinTest {
    private fun resource(path: String): FileHandle {
        val file = File(path)
        return if (file.isAbsolute) FileHandle(file) else FileHandle(File(requireNotNull(javaClass.classLoader.getResource(path)).toURI()))
    }

    private val resolver = mockk<AssetResolver> {
        every { handle(any()) } answers { resource(firstArg()) }
    }
    private val textures = mockk<AssetTextureLoader> {
        every { load(any()) } answers {
            val dimensions = ByteBuffer.wrap(File(firstArg<String>()).readBytes())
            mockk<Texture>(relaxed = true) {
                every { width } returns dimensions.getInt(16)
                every { height } returns dimensions.getInt(20)
            }
        }
        every { loadFont(any(), any()) } answers {
            val data = BitmapFont.BitmapFontData(resource(firstArg()), secondArg())
            val pages = Array<TextureRegion>()
            data.imagePaths.forEach { pages.add(TextureRegion(load(it))) }
            BitmapFont(data, pages, false)
        }
    }

    @Test
    fun `bundled gameplay atlas and JSON resolve every window widget style`() {
        val skin = GameWindowSkin(resolver, textures)
        try {
            for (style in listOf("default", "touch")) {
                assertNotNull(skin.get(style, Window.WindowStyle::class.java).background)
                assertNotNull(skin.get(style, Window.WindowStyle::class.java).stageBackground)
                assertNotNull(skin.get(style, ScrollPane.ScrollPaneStyle::class.java).vScrollKnob)
                assertNotNull(skin.get(style, Label.LabelStyle::class.java).font)
                val field = skin.get(style, TextField.TextFieldStyle::class.java)
                assertNotNull(field.cursor)
                assertNotNull(field.selection)
                assertNotNull(field.focusedBackground)
                assertNotNull(field.disabledBackground)
                assertFalse(field.font.isFlipped)
                assertFalse(field.font.data.markupEnabled)
            }
            for (style in listOf("default", "touch", "teleport", "teleport-touch")) {
                val button = skin.get(style, TextButton.TextButtonStyle::class.java)
                assertNotNull(button.down)
                assertNotNull(button.focused)
                assertNotNull(button.disabled)
            }
            for (style in listOf("close", "previous", "next", "close-touch", "previous-touch", "next-touch")) {
                assertNotNull(skin.get(style, ImageButton.ImageButtonStyle::class.java).imageDisabled)
            }
            assertNotNull(skin.get("destination", Button.ButtonStyle::class.java).checked)
            assertNotNull(skin.get("default-horizontal", ProgressBar.ProgressBarStyle::class.java).knobBefore)
        } finally {
            skin.dispose()
        }
    }

    @Test
    fun `touch controls reserve at least 44 units without enlarging desktop controls`() {
        val skin = GameWindowSkin(resolver, textures)
        try {
            for (style in listOf("touch", "teleport-touch")) {
                val button = skin.get(style, TextButton.TextButtonStyle::class.java)
                listOf(button.up, button.over, button.down, button.focused, button.disabled).forEach { background ->
                    assertTrue(background.minWidth >= 44f)
                    assertTrue(background.minHeight >= 44f)
                }
            }
            for (style in listOf("close-touch", "previous-touch", "next-touch")) {
                assertTrue(skin.get(style, ImageButton.ImageButtonStyle::class.java).up.minHeight >= 44f)
            }
            assertTrue(skin.get("touch", TextField.TextFieldStyle::class.java).background.minHeight >= 44f)
            assertTrue(skin.get("destination-touch", Button.ButtonStyle::class.java).checked.minHeight >= 44f)
            assertTrue(skin.get("default", TextButton.TextButtonStyle::class.java).up.minHeight < 44f)
            assertTrue(skin.get("default", TextField.TextFieldStyle::class.java).background.minHeight < 44f)
        } finally {
            skin.dispose()
        }
    }

    @Test
    fun `gameplay fonts and styles cannot change menu fonts or other gameplay font sizes`() {
        val menu = PackSkin(resolver, textures)
        val game = GameWindowSkin(resolver, textures)
        try {
            val menuFont = menu.getFont("lanapixel")
            val desktop = game.getFont("window-font")
            val touch = game.getFont("touch-font")
            val originalMenuScale = menuFont.data.scaleX
            val originalTouchScale = touch.data.scaleX

            assertNotSame(menuFont, desktop)
            assertNotSame(desktop, touch)
            assertEquals(9f, desktop.capHeight, 0.001f)
            assertEquals(14f, touch.capHeight, 0.001f)
            desktop.data.setScale(2f)
            assertEquals(originalMenuScale, menuFont.data.scaleX)
            assertEquals(originalTouchScale, touch.data.scaleX)
        } finally {
            game.dispose()
            menu.dispose()
        }
    }
}
