package com.sj.stocktracker.data.repository

import com.sj.stocktracker.data.network.WebSocketManager
import com.sj.stocktracker.domain.model.ConnectionStatus
import com.sj.stocktracker.domain.model.PriceChange
import com.sj.stocktracker.domain.model.Stock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class StockRepository @Inject constructor(
    private val webSocketManager: WebSocketManager
) {
    companion object {
        private const val SIMULATION_INTERVAL_MS = 2000L
        private const val MIN_PRICE = 10.0
        private const val MAX_PRICE = 1000.0

        val SYMBOLS = listOf(
            "AAPL", "GOOG", "TSLA", "AMZN", "MSFT",
            "NVDA", "META", "NFLX", "AMD", "INTC",
            "ORCL", "CRM", "ADBE", "PYPL", "SQ",
            "SHOP", "UBER", "LYFT", "SNAP", "PINS",
            "TWLO", "ZM", "DOCU", "PLTR", "COIN"
        )
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _stocks = MutableStateFlow<Map<String, Stock>>(
        SYMBOLS.associateWith { Stock(symbol = it) }
    )

    val stocks: StateFlow<Map<String, Stock>> = _stocks.asStateFlow()

    val connectionStatus: StateFlow<ConnectionStatus> = webSocketManager.connectionStatus

    private var simulationJob: Job? = null
    private var messageCollectionJob: Job? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun startSimulation() {
        if (_isRunning.value) return

        _isRunning.value = true
        webSocketManager.connect()
        startMessageCollection()
        startPriceGeneration()
    }

    fun stopSimulation() {
        _isRunning.value = false
        simulationJob?.cancel()
        simulationJob = null
        messageCollectionJob?.cancel()
        messageCollectionJob = null
        webSocketManager.disconnect()
    }

    private fun startMessageCollection() {
        messageCollectionJob?.cancel()
        messageCollectionJob = webSocketManager.incomingMessages
            .onEach { message -> processMessage(message) }
            .launchIn(scope)
    }

    private fun startPriceGeneration() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            // Brief delay to allow WebSocket connection to establish
            delay(500)
            while (isActive) {
                SYMBOLS.forEach { symbol ->
                    val price = Random.nextDouble(MIN_PRICE, MAX_PRICE)
                    val formattedPrice = "%.2f".format(price)
                    webSocketManager.sendMessage("$symbol:$formattedPrice")
                }
                delay(SIMULATION_INTERVAL_MS)
            }
        }
    }

    private fun processMessage(message: String) {
        val parts = message.split(":")
        if (parts.size != 2) return

        val symbol = parts[0].trim()
        val newPrice = parts[1].trim().toDoubleOrNull() ?: return

        _stocks.update { currentMap ->
            val currentStock = currentMap[symbol] ?: return@update currentMap
            val previousPrice = currentStock.price
            val change = when {
                previousPrice == null -> PriceChange.NONE
                newPrice > previousPrice -> PriceChange.UP
                newPrice < previousPrice -> PriceChange.DOWN
                else -> PriceChange.NONE
            }
            currentMap + (symbol to currentStock.copy(
                price = newPrice,
                previousPrice = previousPrice,
                change = change
            ))
        }
    }
}
