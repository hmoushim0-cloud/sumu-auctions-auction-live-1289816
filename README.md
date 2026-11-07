# تطبيق سمو للمزادات الحية - مزادات الخيل والنوادر الملكية (`sumu-auctions-auction-live-1289816`)
> منظومة auction_live المتكاملة مع ربط BLE Hardware Bidding Paddle GATT + Sub-100ms WebRTC Audio

- **Project ID**: `#1289816`
- **Commercial Archetype**: `auction_live`
- **Bound Hardware Peripheral**: `BLE Hardware Bidding Paddle GATT + Sub-100ms WebRTC Audio`
- **Package Name**: `sa.factory.sumuauctionsauctio`
- **Zero-Emoji Invariant**: Strictly Enforced (100% Vector Icons)

## [Sidekick Autonomous Update]
- **Hardware Integration**:
  - `BleBiddingPaddleProtocolEngine`: BLE GATT protocol engine (Service UUID `0000B1D0-0000-1000-8000-00805F9B34FB`), sub-100ms low-latency bid dispatch, capacitive trigger debounce (< 50ms), battery reporting, and SHA-256 rolling cryptographic nonce verification.
  - `AuctionBiddingRuleEngine`: Live luxury auction management (Arabian horses, heritage daggers, illuminated manuscripts, royal timepieces), multi-tier dynamic bid increments (500 to 10,000 SAR), regulatory reserve price validation, and hammer countdown flow (Going Once, Going Twice, Sold/Reserve Not Met).
- **Hardware Driver**:
  - `HardwareSensorDriver`: Reactive Kotlin Coroutines Flow streaming paddle status, battery telemetry, and real-time auction volume.
- **Quality & Governance**:
  - 10 comprehensive unit regression tests covering paddle triggers, debounce prevention, cryptographic hashing, increment brackets, reserve price compliance, and driver streams (100% assertions green).
  - 100% Zero-Emoji compliance in code, manifests, and UI strings.
