package com.sj.stocktracker.ui.feed

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sj.stocktracker.data.repository.StockRepository
import com.sj.stocktracker.domain.model.FeedUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {
    val uiState = combine(
        repository.stocks,
        repository.connectionStatus,
        repository.isRunning
    ) { stocksMap, connectionStatus, isRunning ->
        FeedUiState(
            stocks = stocksMap.values
                .sortedByDescending { it.price ?: 0.0 },
            connectionStatus = connectionStatus,
            isRunning = isRunning
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FeedUiState()
    )

    init {
        startSimulaton()
    }

    fun startSimulaton() {
        repository.startSimulation()
    }
}