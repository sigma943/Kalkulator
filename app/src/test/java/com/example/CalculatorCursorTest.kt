package com.example

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w393dp-h851dp-port")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CalculatorCursorTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun tappingBetweenDigitsEditsAtThatPosition() {
        fun press(key: String) {
            compose.onNodeWithTag("btn_$key").performTouchInput { click() }
        }

        press("ac")
        listOf("1", "2", "3", "4").forEach(::press)
        val display = compose.onNodeWithTag("display_expression")
        display.performTouchInput { click(center) }
        press("9")
        display.assertTextEquals("12934")
        press("backspace")
        press("plus")
        display.assertTextEquals("12+34")
    }
}
