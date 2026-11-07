package sa.factory.sumuauctionsauctio

import sa.factory.sumuauctionsauctio.data.repository.SumuAuctionsRepositoryImpl
import sa.factory.sumuauctionsauctio.engine.AuctionBiddingRuleEngine
import sa.factory.sumuauctionsauctio.engine.BleBiddingPaddleProtocolEngine
import sa.factory.sumuauctionsauctio.engine.LotStatus
import sa.factory.sumuauctionsauctio.engine.PaddleConnectionState
import sa.factory.sumuauctionsauctio.hardware.HardwareSensorDriver
import sa.factory.sumuauctionsauctio.model.SumuAuctionsItem
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SumuAuctionsRegressionTest {

    @Test
    fun testCheckoutCalculationAndTax() = runBlocking {
        val sensorDriver = HardwareSensorDriver()
        val repository = SumuAuctionsRepositoryImpl(sensorDriver)

        val item1 = SumuAuctionsItem("t1", "خدمة تجريبية أ", "الوصف", 100.0, "معتمد", "عام")
        val item2 = SumuAuctionsItem("t2", "خدمة تجريبية ب", "الوصف", 200.0, "معتمد", "عام")

        repository.addToCart(item1)
        repository.addToCart(item2)

        val order = repository.checkout("Mada EMV")

        assertEquals(300.0, order.subtotalSar, 0.01)
        assertEquals(45.0, order.vatSar, 0.01)
        assertEquals(345.0, order.totalSar, 0.01)
        assertEquals(0, repository.activeCart.value.size)
    }

    @Test
    fun testPaddleInitializationAndBatteryClamping() {
        val paddle = BleBiddingPaddleProtocolEngine("PAD-SA-999", 55)
        assertEquals("PAD-SA-999", paddle.paddleId)
        assertEquals(55, paddle.assignedBidderNumber)
        assertEquals(PaddleConnectionState.PAIRED_READY, paddle.getConnectionState())

        paddle.setBatteryLevel(85)
        assertEquals(85, paddle.getBatteryLevel())

        paddle.setBatteryLevel(130)
        assertEquals(100, paddle.getBatteryLevel())
    }

    @Test
    fun testPaddleBidTriggerAndLatency() {
        val paddle = BleBiddingPaddleProtocolEngine()
        val event = paddle.triggerBid("LOT-01", 100000.0, 5000.0, simulatedLatencyMs = 32)
        assertNotNull(event)
        assertEquals(105000.0, event!!.bidAmountSar, 0.01)
        assertEquals(32L, event.latencyMs)
        assertEquals(PaddleConnectionState.ACKNOWLEDGED, paddle.getConnectionState())
    }

    @Test
    fun testPaddleDebounceClickPrevention() {
        val paddle = BleBiddingPaddleProtocolEngine()
        val event1 = paddle.triggerBid("LOT-01", 50000.0, 2500.0)
        assertNotNull(event1)

        // Instant second click within 50ms must be debounced
        val isDebounced = paddle.isDebounced(System.currentTimeMillis(), debounceWindowMs = 200)
        assertTrue(isDebounced)
    }

    @Test
    fun testPaddleCryptographicSignatureIntegrity() {
        val paddle = BleBiddingPaddleProtocolEngine()
        val hash1 = paddle.generateBidHash("LOT-02", 35000.0, 1001)
        val hash2 = paddle.generateBidHash("LOT-02", 35000.0, 1001)
        val hash3 = paddle.generateBidHash("LOT-02", 35000.0, 1002)

        assertEquals("Hashes with same nonce must match", hash1, hash2)
        assertNotEquals("Hashes with different nonces must differ", hash1, hash3)
        assertEquals(16, hash1.length)
    }

    @Test
    fun testAuctionLotRosterAndStartingPrices() {
        val engine = AuctionBiddingRuleEngine()
        val lots = engine.getLots()
        assertEquals(4, lots.size)

        val horseLot = engine.findLotById("LOT-01")
        assertNotNull(horseLot)
        assertEquals("المهر العربي الأصيل - كحيل الصقلاوي", horseLot?.title)
        assertTrue(horseLot!!.startingPriceSar >= 100000.0)
    }

    @Test
    fun testAuctionMinimumIncrementBrackets() {
        val engine = AuctionBiddingRuleEngine()

        // Tier 1: Under 10k
        assertEquals(500.0, engine.calculateMinimumIncrement(5000.0), 0.01)
        // Tier 2: 10k to 50k
        assertEquals(2500.0, engine.calculateMinimumIncrement(20000.0), 0.01)
        // Tier 3: 50k to 200k
        assertEquals(5000.0, engine.calculateMinimumIncrement(120000.0), 0.01)
        // Tier 4: Over 200k
        assertEquals(10000.0, engine.calculateMinimumIncrement(350000.0), 0.01)
    }

    @Test
    fun testAuctionBidValidationSuccess() {
        val engine = AuctionBiddingRuleEngine()
        val lot = engine.findLotById("LOT-02")
        assertNotNull(lot)

        val minInc = engine.calculateMinimumIncrement(lot!!.currentPriceSar)
        val validBid = lot.currentPriceSar + minInc

        val result = engine.submitBid("LOT-02", 42, validBid)
        assertTrue(result.isAccepted)
        assertEquals(validBid, result.updatedLot?.currentPriceSar ?: 0.0, 0.01)
        assertEquals(42, result.updatedLot?.highestBidderNumber)
    }

    @Test
    fun testAuctionHammerCountdownAndReserveLogic() {
        val engine = AuctionBiddingRuleEngine()
        val lot = engine.findLotById("LOT-03")
        assertNotNull(lot)

        // Advance: ACTIVE -> GOING_ONCE
        var status = engine.advanceHammerCountdown("LOT-03")
        assertEquals(LotStatus.GOING_ONCE, status)

        // Advance: GOING_ONCE -> GOING_TWICE
        status = engine.advanceHammerCountdown("LOT-03")
        assertEquals(LotStatus.GOING_TWICE, status)

        // Final Advance: currentPrice (95,000) < reservePrice (120,000) -> RESERVE_NOT_MET
        status = engine.advanceHammerCountdown("LOT-03")
        assertEquals(LotStatus.RESERVE_NOT_MET, status)
    }

    @Test
    fun testHardwareSensorDriverLiveStreamFrames() = runBlocking {
        val driver = HardwareSensorDriver()
        val frames = driver.observePeripheralStream().take(2).toList()

        assertEquals("CONNECTING", frames[0].connectionState)
        assertEquals("ONLINE", frames[1].connectionState)
        assertTrue(frames[1].livePayload.contains("BLE Hardware Bidding Paddle"))
    }
}
