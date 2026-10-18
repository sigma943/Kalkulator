package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.ui.CalculatorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import androidx.lifecycle.viewModelScope
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CalculatorViewModelTest {
    private lateinit var vm: CalculatorViewModel
    private lateinit var settings: SettingsManager

    @Before fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("calculator_settings", Context.MODE_PRIVATE).edit().clear().commit()
        settings = SettingsManager(context)
        val dao = object : HistoryDao {
            override fun getAllHistory(): Flow<List<HistoryEntity>> = MutableStateFlow(emptyList())
            override suspend fun insert(history: HistoryEntity): Long = 1L
            override suspend fun deleteById(id: Long) {}
            override suspend fun clearAll() {}
        }
        vm = CalculatorViewModel(CalculatorRepository(dao, settings))
    }

    @After fun tearDown() {
        vm.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    private fun input(text: String) { text.forEach { vm.onDigit(it.toString()) } }

    @Test fun roundedResultKeepsPrecisionForContinuation() {
        settings.updateDecimalPlaces(2)
        input("1"); vm.onOperator("÷"); input("3"); vm.onEquals()
        assertEquals("0,33", vm.expression.value)
        vm.onOperator("×"); input("3"); vm.onEquals()
        assertEquals("1,00", vm.expression.value)
    }

    @Test fun digitAfterEqualsStartsNewExpression() {
        input("12"); vm.onOperator("+"); input("3"); vm.onEquals()
        input("7")
        assertEquals("7", vm.expression.value)
    }

    @Test fun negativeOperandCanBeEnteredFromKeypad() {
        input("2"); vm.onOperator("×"); vm.onOperator("−"); input("3"); vm.onEquals()
        assertEquals("-6", vm.expression.value)
    }

    @Test fun negativeExponentCanBeEnteredFromScientificPanel() {
        input("2"); vm.onInsert("^"); vm.onOperator("−"); input("3"); vm.onEquals()
        assertEquals("0,125", vm.expression.value)
    }

    @Test fun scientificResultCanBeContinuedWithoutChangingPrecedence() {
        input("10"); vm.onInsert("^"); input("20"); vm.onEquals()
        vm.onOperator("×"); input("2"); vm.onEquals()
        assertEquals("2×10^20", vm.expression.value)
    }

    @Test fun equalsCompletesParenthesesAndDoesNotTriggerEasterEgg() {
        vm.onInsert("√("); input("16")
        repeat(3) { vm.onEquals() }
        assertEquals("4", vm.expression.value)
        assertFalse(vm.showHogRiderEasterEgg.value)
        vm.triggerHogRiderEasterEgg()
        assertTrue(vm.showHogRiderEasterEgg.value)
    }

    @Test fun percentKeyWorksBeforeAndAfterEquals() {
        input("200"); vm.onOperator("+"); input("10"); vm.onOperator("%"); vm.onEquals()
        assertEquals("220", vm.expression.value)
        vm.onOperator("%"); vm.onEquals()
        assertEquals("2,2", vm.expression.value)
    }

    @Test fun clearAndNewFunctionsDiscardPreviousResult() {
        input("9"); vm.onEquals(); vm.onInsert("√("); input("16"); vm.onEquals()
        assertEquals("4", vm.expression.value)
        vm.onClear(); input("2"); vm.onEquals()
        assertEquals("2", vm.expression.value)
    }
}
