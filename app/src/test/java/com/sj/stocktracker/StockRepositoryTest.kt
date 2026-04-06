package com.sj.stocktracker

import com.sj.stocktracker.data.repository.StockRepository
import com.sj.stocktracker.domain.model.ConnectionStatus
import com.sj.stocktracker.domain.model.PriceChange
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val fakeWsManager = FakeWebSocketManager()
    private lateinit var repository: StockRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        repository = StockRepository(
            webSocketManager = fakeWsManager,
            scope = CoroutineScope(testDispatcher)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial stocks contain all 25 symbols with null price`() {
        val stocks = repository.stocks.value
        assertEquals(25, stocks.size)
        assertTrue(stocks.values.all { it.price == null })
    }

    @Test
    fun `processMessage updates stock price correctly`() = runTest {
        repository.startSimulation()
        fakeWsManager.emitIncoming("AAPL:178.32")

        val stock = repository.stocks.value["AAPL"]
        assertEquals(178.32, stock?.price)

        repository.stopSimulation()
    }

    @Test
    fun `price change is UP when new price is higher`() = runTest {
        repository.startSimulation()

        fakeWsManager.emitIncoming("AAPL:100.00")
        fakeWsManager.emitIncoming("AAPL:150.00")

        val stock = repository.stocks.value["AAPL"]
        assertEquals(PriceChange.UP, stock?.change)

        repository.stopSimulation()
    }

    @Test
    fun `price change is DOWN when new price is lower`() = runTest {
        repository.startSimulation()

        fakeWsManager.emitIncoming("AAPL:150.00")
        fakeWsManager.emitIncoming("AAPL:100.00")

        val stock = repository.stocks.value["AAPL"]
        assertEquals(PriceChange.DOWN, stock?.change)

        repository.stopSimulation()
    }


    @Test
    fun `startSimulation connects websocket`() = runTest {
        repository.startSimulation()
        assertEquals(ConnectionStatus.CONNECTED, fakeWsManager.connectionStatus.value)
        repository.stopSimulation()
    }

    @Test
    fun `stopSimulation disconnects websocket`() = runTest {
        repository.startSimulation()
        repository.stopSimulation()
        assertEquals(ConnectionStatus.DISCONNECTED, fakeWsManager.connectionStatus.value)
    }
}