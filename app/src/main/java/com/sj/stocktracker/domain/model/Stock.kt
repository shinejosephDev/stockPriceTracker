package com.sj.stocktracker.domain.model


data class Stock(
    val symbol: String,
    val price: Double? = null,
    val previousPrice: Double? = null,
    val change: PriceChange = PriceChange.NONE
)

enum class PriceChange {
    UP,
    DOWN,
    NONE
}