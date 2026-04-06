# stockPriceTracker
## 📱 App Preview

![Demo](assets/demo.gif)

| Layer | Tech |
|-------|------|
| UI | Jetpack Compose, Material3 |
| Navigation | Navigation Compose |
| Architecture | MVVM + Repository, unidirectional data flow |
| Async | Kotlin Coroutines, StateFlow |
| Networking | OkHttp WebSocket (`wss://ws.postman-echo.com/raw`) |
| DI | Hilt (KSP) |
| Testing | JUnit4, Coroutines Test, Compose UI Test |

## Architecture

- **`StockRepository`** — owns the stocks map, generates random prices, sends them over WebSocket, parses echoed messages back into state updates.
- **`IWebSocketManager`** — abstraction over OkHttp WebSocket; `FakeWebSocketManager` used in tests.
- **ViewModels** (`FeedViewModel`, `DetailsViewModel`) — expose `StateFlow<UiState>` to Compose, delegate actions to repository.

## App Flow

1. **Feed screen** displays a list of predefined stock symbols with live prices.
2. User taps **Start** → `repository.startSimulation()` opens WebSocket and begins emitting random price messages (`"SYMBOL:PRICE"`).
3. Prices are sent to Postman echo WebSocket and received back — parsed into the stocks `StateFlow` map.
4. Each stock row shows price, change direction (up/down flash animation), and connection status indicator.
5. Tapping a stock row navigates to **Details screen** (`details/{symbol}`) showing current price, change chip, and a static company description.
6. User can **Stop** the feed, which closes the WebSocket and resets connection state.

## Deep Links
The app supports deep linking into the Details screen via a custom URI scheme.
**URI pattern:** `stocks://symbol/{symbol}`
Example: `stocks://symbol/AAPL` opens the Details screen for Apple.
```
adb shell am start -a android.intent.action.VIEW -d "stocks://symbol/AAPL"
```

### Unit tests

![UI Tests](assets/test1.png)

- **Unit** (`StockRepositoryTest`) — initial state, message parsing, price change direction, start/stop lifecycle. Uses `FakeWebSocketManager`.


---

## Compose UI Tests

![Coverage](assets/test2.png)

- **UI** (`FeedScreenTest`) — stock row rendering, connection indicator semantics, start/stop button states, navigation callbacks.

