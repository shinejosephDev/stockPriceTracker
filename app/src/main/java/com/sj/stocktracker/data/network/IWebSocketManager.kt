package com.sj.stocktracker.data.network

import com.sj.stocktracker.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IWebSocketManager {
    val connectionStatus: StateFlow<ConnectionStatus>
    val incomingMessages: SharedFlow<String>
    fun connect()
    fun disconnect()
    fun sendMessage(message: String): Boolean
}