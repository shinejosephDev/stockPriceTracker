package com.sj.stocktracker.domain.model


data class FeedUiState(
    val stocks: List<Stock> = emptyList(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val isRunning: Boolean = false
)


enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING
}

data class DetailsUiState(
    val stock: Stock? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val description: String = ""
)