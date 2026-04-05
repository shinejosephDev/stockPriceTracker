@file:OptIn(ExperimentalMaterial3Api::class)

package com.sj.stocktracker.ui.details

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sj.stocktracker.domain.model.ConnectionStatus
import com.sj.stocktracker.domain.model.DetailsUiState
import com.sj.stocktracker.domain.model.PriceChange
import com.sj.stocktracker.domain.model.Stock
import kotlinx.coroutines.delay

@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetailsContent(
        uiState,
        onNavigateBack,
        modifier
    )
}

@Composable
fun DetailsContent(
    uiState: DetailsUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.stock?.symbol ?: "Stock Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = ""
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        val stock = uiState.stock

        if (stock == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Stock not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PriceCard(
                    price = stock.price,
                    change = stock.change,
                    connectionStatus = uiState.connectionStatus
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "About",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Market Info",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(label = "Symbol", value = stock.symbol)
                        InfoRow(
                            label = "Previous Price",
                            value = stock.previousPrice?.let { "$${"%,.2f".format(it)}" } ?: "—"
                        )
                        InfoRow(
                            label = "Status",
                            value = when (uiState.connectionStatus) {
                                ConnectionStatus.CONNECTED -> "Live"
                                ConnectionStatus.CONNECTING -> "Connecting..."
                                ConnectionStatus.DISCONNECTED -> "Offline"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceCard(
    price: Double?,
    change: PriceChange,
    connectionStatus: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    var isFlashing by remember { mutableStateOf(false) }
    val flashColor = when (change) {
        PriceChange.UP -> Color(0x2200C853)
        PriceChange.DOWN -> Color(0x22FF1744)
        PriceChange.NONE -> Color.Transparent
    }

    LaunchedEffect(price) {
        if (change != PriceChange.NONE) {
            isFlashing = true
            delay(1000)
            isFlashing = false
        }
    }

    val animatedColor by animateColorAsState(
        targetValue = if (isFlashing) flashColor else Color.Transparent,
        animationSpec = tween(durationMillis = 500),
        label = "detailFlash"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(animatedColor)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when (connectionStatus) {
                                ConnectionStatus.CONNECTED -> Color(0xFF00C853)
                                ConnectionStatus.CONNECTING -> Color(0xFFFFC107)
                                ConnectionStatus.DISCONNECTED -> Color(0xFFFF1744)
                            }
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> "Live"
                        ConnectionStatus.CONNECTING -> "Connecting"
                        ConnectionStatus.DISCONNECTED -> "Offline"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            price?.let {
                Text(
                    text = "$${"%,.2f".format(it)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } ?: Text(
                text = "Waiting for data...",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            val (label, chipColor) = when (change) {
                PriceChange.UP -> "↑ Price Up" to Color(0xFF00C853)
                PriceChange.DOWN -> "↓ Price Down" to Color(0xFFFF1744)
                PriceChange.NONE -> "— No Change" to MaterialTheme.colorScheme.onSurfaceVariant
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = chipColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(chipColor.copy(alpha = 0.12f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
fun DetailsContentPreview() {

    val previewUiState = DetailsUiState(
        stock = Stock(
            symbol = "AAPL",
            price = 192.33,
            change = PriceChange.UP,
            previousPrice = 100.0
        ),
        description = "This is a test description"
    )

    DetailsContent(
        uiState = previewUiState,
        onNavigateBack = {},
    )
}