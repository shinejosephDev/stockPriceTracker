@file:OptIn(ExperimentalMaterial3Api::class)

package com.sj.stocktracker.ui.feed

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sj.stocktracker.domain.model.ConnectionStatus
import com.sj.stocktracker.domain.model.FeedUiState
import com.sj.stocktracker.domain.model.PriceChange
import com.sj.stocktracker.domain.model.Stock
import kotlinx.coroutines.delay

@Composable
fun FeedScreen(
    onStockClick: (String) -> Unit,
    viewModel: FeedViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FeedContent(
        uiState, onStockClick,    viewModel::toggleSimulation,modifier
    )
}

@Composable
fun FeedContent(
    uiState: FeedUiState,
    onStockClick: (String) -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ConnectionIndicator(status = uiState.connectionStatus, modifier)
                        Spacer(modifier = modifier.width(12.dp))
                        Text(
                            text = "Price Tracker",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onToggle,
                        modifier = Modifier.testTag("toggle_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isRunning) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    ) {
                        Text(if (uiState.isRunning) "Stop" else "Start")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("stock_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 12.dp
            )
        ) {
            items(
                items = uiState.stocks
            ) { stock ->
                StockRow(
                    stock = stock,
                    onClick = { onStockClick(stock.symbol) },
                    modifier
                )
            }
        }
    }
}

@Composable
fun StockRow(stock: Stock, onClick: () -> Unit, modifier: Modifier = Modifier) {

    var isFlashing by remember { mutableStateOf(false) }

    LaunchedEffect(stock.price) {
        if (stock.change != PriceChange.NONE) {
            isFlashing = true
            delay(1000)
            isFlashing = false
        }
    }

    val flashColor = when (stock.change) {
        PriceChange.UP -> Color(0xFF00C853)
        PriceChange.DOWN -> Color(0xFFFF1744)
        PriceChange.NONE -> Color.Transparent
    }

    val animatedColor by animateColorAsState(
        targetValue = if (isFlashing) flashColor else Color.Transparent,
        animationSpec = tween(durationMillis = 500),
        label = "flashAnimation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stock_row_${stock.symbol}")
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stock.symbol,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = animatedColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                stock.price?.let { price ->
                    Text(
                        text = "$${"%,.2f".format(price)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PriceChangeIndicator(change = stock.change, modifier)
                } ?: Text(
                    text = "—",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PriceChangeIndicator(
    change: PriceChange,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (change) {
        PriceChange.UP -> "↑" to Color.White
        PriceChange.DOWN -> "↓" to Color.White
        PriceChange.NONE -> "—" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
private fun ConnectionIndicator(
    status: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        ConnectionStatus.CONNECTED -> Color(0xFF00C853)
        ConnectionStatus.CONNECTING -> Color(0xFFFFC107)
        ConnectionStatus.DISCONNECTED -> Color(0xFFFF1744)
    }
    val description = when (status) {
        ConnectionStatus.CONNECTED -> "Connected"
        ConnectionStatus.CONNECTING -> "Connecting"
        ConnectionStatus.DISCONNECTED -> "Disconnected"
    }
    Box(
        modifier = modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color)
            .testTag("connection_indicator")
            .semantics { contentDescription = description }
    )
}


@Preview
@Composable
fun FeedContentPreview() {
    val sampleStocks = listOf(
        Stock(symbol = "AAPL", price = 192.33, change = PriceChange.UP, previousPrice = 100.0),
        Stock(symbol = "GOOG", price = 2875.48),
        Stock(symbol = "TSLA", price = 812.75)
    )
    val previewUiState = FeedUiState(
        stocks = sampleStocks
    )

    FeedContent(
        uiState = previewUiState,
        onStockClick = {},
        onToggle = {}
    )
}