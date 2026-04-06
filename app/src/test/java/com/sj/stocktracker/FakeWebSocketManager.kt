package com.sj.stocktracker

import com.sj.stocktracker.data.network.IWebSocketManager
import com.sj.stocktracker.domain.model.ConnectionStatus
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeWebSocketManager : IWebSocketManager {
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    override val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<String>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val incomingMessages: SharedFlow<String> = _incomingMessages.asSharedFlow()

    val sentMessages = mutableListOf<String>()
    var shouldEcho = true
    var shouldConnect = true

    override fun connect() {
        _connectionStatus.value = if (shouldConnect) {
            ConnectionStatus.CONNECTED
        } else {
            ConnectionStatus.DISCONNECTED
        }
    }

    override fun disconnect() {
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
    }

    override fun sendMessage(message: String): Boolean {
        if (_connectionStatus.value != ConnectionStatus.CONNECTED) return false
        sentMessages.add(message)
        if (shouldEcho) {
            _incomingMessages.tryEmit(message)
        }
        return true
    }

    fun emitIncoming(message: String) {
        _incomingMessages.tryEmit(message)
    }
}