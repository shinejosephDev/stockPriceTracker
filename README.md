# stockPriceTracker
## 📱 App Preview

![Demo](assets/demo.gif)

## ✨ Features

- Real-time stock price updates via WebSocket
- Single source of truth with StateFlow
- MVVM architecture
- Navigation Compose (Feed → Details)
- Start/Stop live feed
- Connection status indicator

### Unit tests

![UI Tests](assets/test1.png)

Tests for `StockRepository` business logic using JUnit4 and a `FakeWebSocketManager`.
No Android dependencies — runs on the local JVM.


---

## Compose UI Tests

![Coverage](assets/test2.png)

Tests for the Feed screen UI using Compose Testing APIs.
Runs on a connected device or emulator.
