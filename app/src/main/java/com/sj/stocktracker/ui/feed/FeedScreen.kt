package com.sj.stocktracker.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FeedScreen(
    onStockClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold() { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 12.dp
            )
        ) {
            items(
                items = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            ) { index ->
                Text(
                    text = "Hello items $index",
                    modifier = modifier.clickable {
                        onStockClick.invoke("$index")
                    }
                )
            }
        }
    }
}