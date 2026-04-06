package com.sj.stocktracker.data.network

import android.util.Log
import com.sj.stocktracker.core.di.ApplicationScope
import com.sj.stocktracker.domain.model.ConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
    @ApplicationScope private val reconnectScope: CoroutineScope
) : IWebSocketManager{
    companion object {
        private const val TAG = "WebSocketManager"
        private const val WS_URL = "wss://ws.postman-echo.com/raw"
        private const val RECONNECT_DELAY_MS = 3000L
        private const val MAX_RETRY_ATTEMPTS = 5
    }

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // No timeout for WebSocket
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var retryCount = 0

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    override val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<String>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val incomingMessages: SharedFlow<String> = _incomingMessages.asSharedFlow()

    private val listener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket connected")
            _connectionStatus.value = ConnectionStatus.CONNECTED
            retryCount = 0
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Received: $text")
            _incomingMessages.tryEmit(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closing: $code / $reason")
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closed: $code / $reason")
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket failure: ${t.message}", t)
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
            attemptReconnect()
        }
    }

    override fun connect() {
        if (_connectionStatus.value == ConnectionStatus.CONNECTED ||
            _connectionStatus.value == ConnectionStatus.CONNECTING
        ) return
        isUserDisconnected = false
        _connectionStatus.value = ConnectionStatus.CONNECTING
        val request = Request.Builder().url(WebSocketManager.Companion.WS_URL).build()
        webSocket = client.newWebSocket(request, listener)
    }


    override fun disconnect() {
        isUserDisconnected = true

        retryCount = 0
        webSocket?.close(1000, "User requested disconnect")
        webSocket = null
    }

    override fun sendMessage(message: String): Boolean {
        return webSocket?.send(message) ?: false
    }

    private var isUserDisconnected =
        false

    private fun attemptReconnect() {
        if (isUserDisconnected) return
        if (retryCount >= MAX_RETRY_ATTEMPTS) {
            Log.w(TAG, "Max retry attempts reached")
            return
        }
        retryCount++
        val delay = RECONNECT_DELAY_MS * retryCount
        Log.d(TAG, "Reconnecting in ${delay}ms (attempt $retryCount)")

        reconnectScope.launch {
            delay(delay)
            connect()
        }
    }
}
