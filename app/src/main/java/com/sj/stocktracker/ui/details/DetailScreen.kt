package com.sj.stocktracker.ui.details

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
) {
    Text(
        text = "Hello DetailScreen",
        modifier = modifier
    )
}