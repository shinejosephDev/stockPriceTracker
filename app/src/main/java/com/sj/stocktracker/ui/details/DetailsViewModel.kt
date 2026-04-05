package com.sj.stocktracker.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sj.stocktracker.data.repository.StockRepository
import com.sj.stocktracker.domain.model.DetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    repository: StockRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(){

    private val symbol: String = checkNotNull(savedStateHandle["symbol"])

    val uiState = combine(
        repository.stocks,
        repository.connectionStatus
    ) { stocksMap, connectionStatus ->
        DetailsUiState(
            stock = stocksMap[symbol],
            connectionStatus = connectionStatus,
            description = generateDescription(symbol)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetailsUiState()
    )

    private fun generateDescription(symbol: String): String = when (symbol) {
        "AAPL" -> "Apple Inc. designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories worldwide."
        "GOOG" -> "Alphabet Inc. offers various products and platforms in the United States, Europe, the Middle East, Africa, the Asia-Pacific, Canada, and Latin America."
        "TSLA" -> "Tesla, Inc. designs, develops, manufactures, leases, and sells electric vehicles, and energy generation and storage systems."
        "AMZN" -> "Amazon.com, Inc. engages in the retail sale of consumer products, advertising, and subscription services through online and physical stores."
        "MSFT" -> "Microsoft Corporation develops and supports software, services, devices, and solutions worldwide."
        "NVDA" -> "NVIDIA Corporation provides graphics and compute networking solutions in the United States, Taiwan, China, Hong Kong, and internationally."
        "META" -> "Meta Platforms, Inc. engages in the development of products that enable people to connect through mobile devices, PCs, virtual reality headsets, and wearables."
        "NFLX" -> "Netflix, Inc. provides entertainment services. It offers TV series, documentaries, feature films, and games across various genres and languages."
        "AMD" -> "Advanced Micro Devices, Inc. operates as a semiconductor company worldwide, offering x86 microprocessors and GPUs."
        "INTC" -> "Intel Corporation designs, develops, manufactures, and sells computing and related products and services worldwide."
        "ORCL" -> "Oracle Corporation offers products and services that address enterprise IT environments worldwide."
        "CRM" -> "Salesforce, Inc. provides customer relationship management technology that brings companies and customers together."
        "ADBE" -> "Adobe Inc. operates as a diversified software company worldwide, known for Creative Cloud and Document Cloud."
        "PYPL" -> "PayPal Holdings, Inc. operates a technology platform for digital payments on behalf of merchants and consumers."
        "SQ" -> "Block, Inc. creates tools that enable sellers to accept card payments and provides financial services."
        "SHOP" -> "Shopify Inc. provides a commerce platform and services in Canada, the United States, Europe, the Middle East, Africa, and internationally."
        "UBER" -> "Uber Technologies, Inc. develops and operates proprietary technology applications for ride-sharing, food delivery, and freight."
        "LYFT" -> "Lyft, Inc. operates a peer-to-peer marketplace for on-demand ridesharing in the United States and Canada."
        "SNAP" -> "Snap Inc. operates as a technology company offering Snapchat, a visual messaging application."
        "PINS" -> "Pinterest, Inc. operates as a visual discovery engine in the United States and internationally."
        "TWLO" -> "Twilio Inc. provides a cloud communications platform that enables developers to build, scale, and operate customer engagement."
        "ZM" -> "Zoom Video Communications, Inc. provides a unified communications platform for video, voice, chat, and webinars."
        "DOCU" -> "DocuSign, Inc. provides electronic signature solutions as part of its broader Agreement Cloud platform."
        "PLTR" -> "Palantir Technologies Inc. builds and deploys software platforms for the intelligence community and commercial enterprises."
        "COIN" -> "Coinbase Global, Inc. provides financial infrastructure and technology for the cryptocurrency economy."
        else -> "A publicly traded company listed on major stock exchanges."
    }
}