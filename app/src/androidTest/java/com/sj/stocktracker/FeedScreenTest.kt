package com.sj.stocktracker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sj.stocktracker.domain.model.ConnectionStatus
import com.sj.stocktracker.domain.model.FeedUiState
import com.sj.stocktracker.domain.model.PriceChange
import com.sj.stocktracker.domain.model.Stock
import com.sj.stocktracker.ui.feed.FeedContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FeedScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleStocks = listOf(
        Stock(symbol = "AAPL", price = 192.33, change = PriceChange.UP, previousPrice = 180.0),
        Stock(symbol = "GOOG", price = 2875.48, change = PriceChange.NONE),
        Stock(symbol = "TSLA", price = 812.75, change = PriceChange.DOWN, previousPrice = 900.0)
    )

    @Test
    fun threeSymbolsAreDisplayedWithCorrectText() {
        composeTestRule.setContent {
            FeedContent(
                uiState = FeedUiState(stocks = sampleStocks),
                onStockClick = {},
                onToggle = {}
            )
        }

        composeTestRule.onNodeWithTag("stock_row_AAPL").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stock_row_GOOG").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stock_row_TSLA").assertIsDisplayed()

        composeTestRule.onNodeWithText("AAPL").assertIsDisplayed()
        composeTestRule.onNodeWithText("GOOG").assertIsDisplayed()
        composeTestRule.onNodeWithText("TSLA").assertIsDisplayed()

        composeTestRule.onNodeWithText("$192.33").assertIsDisplayed()
        composeTestRule.onNodeWithText("$2,875.48").assertIsDisplayed()
        composeTestRule.onNodeWithText("$812.75").assertIsDisplayed()
    }

    @Test
    fun connectionIndicatorShowsConnectedState() {
        composeTestRule.setContent {
            FeedContent(
                uiState = FeedUiState(
                    stocks = sampleStocks,
                    connectionStatus = ConnectionStatus.CONNECTED
                ),
                onStockClick = {},
                onToggle = {}
            )
        }

        composeTestRule
            .onNodeWithTag("connection_indicator")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Connected")
            .assertIsDisplayed()
    }

    @Test
    fun connectionIndicatorShowsDisconnectedState() {
        composeTestRule.setContent {
            FeedContent(
                uiState = FeedUiState(
                    stocks = sampleStocks,
                    connectionStatus = ConnectionStatus.DISCONNECTED
                ),
                onStockClick = {},
                onToggle = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Disconnected")
            .assertIsDisplayed()
    }

    @Test
    fun toggleButtonShowsStartWhenNotRunning() {
        composeTestRule.setContent {
            FeedContent(
                uiState = FeedUiState(
                    stocks = sampleStocks,
                    isRunning = false
                ),
                onStockClick = {},
                onToggle = {}
            )
        }

        composeTestRule
            .onNodeWithTag("toggle_button")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Start")
            .assertIsDisplayed()
    }

    @Test
    fun toggleButtonShowsStopWhenRunning() {
        composeTestRule.setContent {
            FeedContent(
                uiState = FeedUiState(
                    stocks = sampleStocks,
                    isRunning = true
                ),
                onStockClick = {},
                onToggle = {}
            )
        }

        composeTestRule
            .onNodeWithText("Stop")
            .assertIsDisplayed()
    }

    @Test
    fun toggleButtonCallsOnToggleWhenClicked() {
        var toggleCount = 0

        composeTestRule.setContent {
            FeedContent(
                uiState = FeedUiState(stocks = sampleStocks),
                onStockClick = {},
                onToggle = { toggleCount++ }
            )
        }

        composeTestRule
            .onNodeWithTag("toggle_button")
            .performClick()

        assertEquals(1, toggleCount)
    }

    @Test
    fun clickingStockRowTriggersNavigationWithCorrectSymbol() {
        var navigatedSymbol: String? = null

        composeTestRule.setContent {
            FeedContent(
                uiState = FeedUiState(stocks = sampleStocks),
                onStockClick = { symbol -> navigatedSymbol = symbol },
                onToggle = {}
            )
        }

        composeTestRule
            .onNodeWithTag("stock_row_AAPL")
            .performClick()

        assertEquals("AAPL", navigatedSymbol)
    }

    @Test
    fun clickingDifferentStockRowsNavigatesToCorrectSymbols() {
        val navigatedSymbols = mutableListOf<String>()

        composeTestRule.setContent {
            FeedContent(
                uiState = FeedUiState(stocks = sampleStocks),
                onStockClick = { symbol -> navigatedSymbols.add(symbol) },
                onToggle = {}
            )
        }

        composeTestRule.onNodeWithTag("stock_row_GOOG").performClick()
        composeTestRule.onNodeWithTag("stock_row_TSLA").performClick()

        assertEquals(listOf("GOOG", "TSLA"), navigatedSymbols)
    }
}
