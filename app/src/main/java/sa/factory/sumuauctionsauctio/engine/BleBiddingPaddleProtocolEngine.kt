package sa.factory.sumuauctionsauctio.engine

import java.security.MessageDigest
import kotlin.math.max
import kotlin.math.min

enum class PaddleConnectionState {
    DISCONNECTED,
    CONNECTING,
    PAIRED_READY,
    BID_TRANSMITTING,
    ACKNOWLEDGED,
    ERROR
}

data class PaddleBidEvent(
    val eventId: String,
    val paddleId: String,
    val bidderNumber: Int,
    val lotId: String,
    val bidAmountSar: Double,
    val latencyMs: Long,
    val cryptographicHash: String,
    val timestamp: Long = System.currentTimeMillis()
)

class BleBiddingPaddleProtocolEngine(
    val paddleId: String = "PAD-SA-778",
    val assignedBidderNumber: Int = 42
) {
    val serviceUuid: String = "0000B1D0-0000-1000-8000-00805F9B34FB"
    val bidCharacteristicUuid: String = "0000B1D1-0000-1000-8000-00805F9B34FB"
    val telemetryCharacteristicUuid: String = "0000B1D2-0000-1000-8000-00805F9B34FB"

    private var connectionState: PaddleConnectionState = PaddleConnectionState.PAIRED_READY
    private var batteryLevelPct: Int = 96
    private var lastTriggerTimestamp: Long = 0
    private var rollingNonce: Long = 1000
    private val bidLog = mutableListOf<PaddleBidEvent>()

    fun getConnectionState(): PaddleConnectionState = connectionState
    fun getBatteryLevel(): Int = batteryLevelPct
    fun getBidHistory(): List<PaddleBidEvent> = bidLog.toList()

    fun setBatteryLevel(pct: Int) {
        batteryLevelPct = max(0, min(100, pct))
    }

    fun isDebounced(triggerTime: Long, debounceWindowMs: Long = 50): Boolean {
        return (triggerTime - lastTriggerTimestamp) < debounceWindowMs
    }

    fun generateBidHash(lotId: String, amountSar: Double, nonce: Long): String {
        val payload = "$paddleId:$assignedBidderNumber:$lotId:$amountSar:$nonce:SUMU-SECRET"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(payload.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }.take(16)
    }

    fun triggerBid(
        lotId: String,
        currentLotPrice: Double,
        incrementSar: Double,
        simulatedLatencyMs: Long = 28
    ): PaddleBidEvent? {
        val now = System.currentTimeMillis()
        if (isDebounced(now)) {
            return null // Reject bounced click
        }
        lastTriggerTimestamp = now
        connectionState = PaddleConnectionState.BID_TRANSMITTING

        rollingNonce++
        val offeredBid = currentLotPrice + incrementSar
        val hash = generateBidHash(lotId, offeredBid, rollingNonce)

        val event = PaddleBidEvent(
            eventId = "BID-${rollingNonce}",
            paddleId = paddleId,
            bidderNumber = assignedBidderNumber,
            lotId = lotId,
            bidAmountSar = offeredBid,
            latencyMs = simulatedLatencyMs,
            cryptographicHash = hash,
            timestamp = now
        )

        bidLog.add(event)
        connectionState = PaddleConnectionState.ACKNOWLEDGED
        return event
    }

    fun resetStateToReady() {
        connectionState = PaddleConnectionState.PAIRED_READY
    }
}
