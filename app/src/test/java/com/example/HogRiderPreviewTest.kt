package com.example

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
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
    @get:Rule val compose = createComposeRule()

    private fun preview(name: String) {
        compose.onNodeWithTag("hog_rider_overlay").captureToImage().asAndroidBitmap().let { bitmap ->
            val file = File("build/reports/hog-rider-previews/$name.png")
            file.parentFile?.mkdirs()
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            assertTrue(file.length() > 1000)
        }
    }

    @Test fun entranceTapAndClose() {
        var dismissed = false
        compose.mainClock.autoAdvance = false
        compose.setContent { MaterialTheme {
            HogRiderEasterEggOverlay(onDismiss = { dismissed = true }, soundEnabled = false, vibrateEnabled = false)
        } }
        compose.mainClock.advanceTimeBy(1900)
        preview("portrait")
        compose.onNodeWithTag("hog_rider_portrait").performClick()
        compose.mainClock.advanceTimeBy(240)
        preview("tap-sparks")
        compose.onNodeWithTag("hog_rider_close").performClick()
        assertTrue(dismissed)
    }

    @Test @Config(qualifiers = "w891dp-h411dp-land") fun landscapeAndAutomaticExit() {
        var dismissed = false
        compose.mainClock.autoAdvance = false
        compose.setContent { MaterialTheme {
            HogRiderEasterEggOverlay(onDismiss = { dismissed = true }, soundEnabled = false, vibrateEnabled = false)
        } }
        compose.mainClock.advanceTimeBy(1900)
        preview("landscape")
        compose.mainClock.advanceTimeBy(5000)
        assertTrue(dismissed)
    }
}
