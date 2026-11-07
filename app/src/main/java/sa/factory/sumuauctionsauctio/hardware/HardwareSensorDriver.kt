package sa.factory.sumuauctionsauctio.hardware

import sa.factory.sumuauctionsauctio.engine.AuctionBiddingRuleEngine
import sa.factory.sumuauctionsauctio.engine.BleBiddingPaddleProtocolEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

data class HardwareTelemetryFrame(
    val peripheralName: String,
    val connectionState: String,
    val livePayload: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HardwareSensorDriver(
    val paddleEngine: BleBiddingPaddleProtocolEngine = BleBiddingPaddleProtocolEngine(),
    val auctionEngine: AuctionBiddingRuleEngine = AuctionBiddingRuleEngine()
) {
    private val peripheral = "BLE Hardware Bidding Paddle GATT + Sub-100ms WebRTC Audio"

    init {
        // Pre-warm sample simulation data
        val lot = auctionEngine.findLotById("LOT-01")
        if (lot != null) {
            val minInc = auctionEngine.calculateMinimumIncrement(lot.currentPriceSar)
            val bidEvent = paddleEngine.triggerBid(lot.lotId, lot.currentPriceSar, minInc, simulatedLatencyMs = 24)
            if (bidEvent != null) {
                auctionEngine.submitBid(lot.lotId, bidEvent.bidderNumber, bidEvent.bidAmountSar)
            }
        }
    }

    fun observePeripheralStream(): Flow<HardwareTelemetryFrame> = flow {
        emit(HardwareTelemetryFrame(peripheral, "CONNECTING", "Handshake initializing..."))
        delay(300)
        emit(HardwareTelemetryFrame(peripheral, "ONLINE", "BLE Hardware Bidding Paddle GATT + Sub-100ms WebRTC Audio: ONLINE | READY"))

        while (true) {
            delay(1500)
            val jitter = Random.nextInt(1, 6)
            val paddleState = paddleEngine.getConnectionState()
            val battery = paddleEngine.getBatteryLevel()
            val totalVolume = auctionEngine.getTotalAuctionVolumeSar()
            val bidsCount = paddleEngine.getBidHistory().size

            val payload = "Paddle: $paddleState (#${paddleEngine.assignedBidderNumber}) | Batt: $battery% | Bids: $bidsCount | Volume: ${String.format("%.0f", totalVolume)} SAR (Latency: ${20 + jitter}ms)"
            emit(HardwareTelemetryFrame(peripheral, "ACTIVE_STREAMING", payload))
        }
    }
}
