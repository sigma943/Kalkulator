package com.example

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.ui.components.HogRiderEasterEggOverlay
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-port")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HogRiderPreviewTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun preview(name: String) {
        compose.onNodeWithTag("hog_rider_overlay").assertIsDisplayed()
        // PixelCopy waits for a hardware frame that Robolectric does not produce.
        // Draw the real Android view hierarchy into a software bitmap instead.
        compose.runOnIdle {
            val view = compose.activity.window.decorView
            assertTrue(view.width > 0 && view.height > 0)
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            val file = File("build/reports/hog-rider-previews/$name.png")
            file.parentFile?.mkdirs()
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            assertTrue(file.length() > 1000)
        }
    }

    @Test fun entranceTapAndClose() {
        var dismissed = false
        compose.mainClock.autoAdvance = false
        compose.setContent { MaterialTheme {
            HogRiderEasterEggOverlay(onDismiss = { dismissed = true }, soundEnabled = false, vibrateEnabled = false)
        } }
        compose.mainClock.advanceTimeBy(1400)
        preview("portrait-fire-lasers")
        listOf("SEKRET ODBLOKOWANY", "HOG RIDER", "Mały sekret. Wielkie wejście.", "DOTKNIJ JEŹDŹCA").forEach {
            compose.onNodeWithText(it).assertDoesNotExist()
        }
        compose.onNodeWithTag("hog_rider_portrait").performClick()
        compose.mainClock.advanceTimeBy(240)
        preview("tap-fire-burst")
        compose.onNodeWithTag("hog_rider_close").performClick()
        assertTrue(dismissed)
    }

    @Test @Config(qualifiers = "w891dp-h411dp-land") fun landscapeAndAutomaticExit() {
        var dismissed = false
        compose.mainClock.autoAdvance = false
        compose.setContent { MaterialTheme {
            HogRiderEasterEggOverlay(onDismiss = { dismissed = true }, soundEnabled = false, vibrateEnabled = false)
        } }
        compose.mainClock.advanceTimeBy(1400)
        preview("landscape")
        compose.mainClock.advanceTimeBy(5500)
        assertTrue(dismissed)
    }
}
