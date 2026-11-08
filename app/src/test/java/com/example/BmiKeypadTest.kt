package com.example

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.example.converter.BmiDetailView
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w393dp-h851dp-port")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BmiKeypadTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun sharedKeypadEditsTheSelectedBmiFieldAndUpdatesResult() {
        compose.setContent {
            MyApplicationTheme {
                BmiDetailView(onBack = {}, vibrateEnabled = false, soundEnabled = false)
            }
        }

        fun press(vararg keys: String) {
            for (key in keys) compose.onNodeWithTag("conv_btn_$key").performTouchInput { click() }
        }

        val weight = compose.onNodeWithTag("bmi_weight")
        val height = compose.onNodeWithTag("bmi_height")
        val result = compose.onNodeWithTag("bmi_result")
        weight.assertIsSelected()
        press("7", "2", "comma", "5", "equals")
        height.assertIsSelected()
        press("1", "8", "0")
        result.assertTextEquals("22,38")

        press("swap")
        weight.assertIsSelected()
        press("backspace")
        result.assertTextEquals("22,22")

        height.performClick()
        height.assertIsSelected()
        press("clear")
        result.assertTextEquals("—")
    }
}
