package sa.factory.sumuauctionsauctio.engine

enum class LotStatus {
    UPCOMING,
    ACTIVE_BIDDING,
    GOING_ONCE,
    GOING_TWICE,
    SOLD,
    RESERVE_NOT_MET
}

data class AuctionLot(
    val lotId: String,
    val title: String,
    val category: String,
    val startingPriceSar: Double,
    val reservePriceSar: Double,
    val currentPriceSar: Double,
    val highestBidderNumber: Int? = null,
    val totalBidsCount: Int = 0,
    val status: LotStatus = LotStatus.ACTIVE_BIDDING
)

data class BidValidationResult(
    val isAccepted: Boolean,
    val updatedLot: AuctionLot?,
    val rejectionReason: String? = null
)

class AuctionBiddingRuleEngine {
    private val lots = mutableListOf(
        AuctionLot("LOT-01", "المهر العربي الأصيل - كحيل الصقلاوي", "الخيول الأصيلة", 150000.0, 300000.0, 180000.0, 42, 6, LotStatus.ACTIVE_BIDDING),
        AuctionLot("LOT-02", "خنجر تراثي مذهب من العصر العثماني", "النوادر والتحف", 25000.0, 45000.0, 32500.0, 19, 4, LotStatus.ACTIVE_BIDDING),
        AuctionLot("LOT-03", "مخطوطة قرآنية بخط الثلث من القرن الرابع الهجري", "المخطوطات الأثرية", 80000.0, 120000.0, 95000.0, 7, 3, LotStatus.ACTIVE_BIDDING),
        AuctionLot("LOT-04", "ساعة ملكية نادرة مرصعة بالزمرد", "المقتنيات الملكية", 350000.0, 500000.0, 420000.0, 88, 8, LotStatus.ACTIVE_BIDDING)
    )

    fun getLots(): List<AuctionLot> = lots.toList()
    fun findLotById(id: String): AuctionLot? = lots.find { it.lotId == id }

    fun calculateMinimumIncrement(currentPriceSar: Double): Double {
        return when {
            currentPriceSar < 10000.0 -> 500.0
            currentPriceSar < 50000.0 -> 2500.0
            currentPriceSar < 200000.0 -> 5000.0
            else -> 10000.0
        }
    }

    fun submitBid(lotId: String, bidderNumber: Int, proposedAmountSar: Double): BidValidationResult {
        val index = lots.indexOfFirst { it.lotId == lotId }
        if (index == -1) {
            return BidValidationResult(false, null, "المعروض غير موجود")
        }

        val lot = lots[index]
        if (lot.status != LotStatus.ACTIVE_BIDDING &&
            lot.status != LotStatus.GOING_ONCE &&
            lot.status != LotStatus.GOING_TWICE
        ) {
            return BidValidationResult(false, null, "المزاد مغلق على هذا المعروض")
        }

        val minIncrement = calculateMinimumIncrement(lot.currentPriceSar)
        val minimumAcceptableBid = lot.currentPriceSar + minIncrement

        if (proposedAmountSar < minimumAcceptableBid) {
            return BidValidationResult(
                false,
                lot,
                "قيمة العطاء أدنى من الحد الأدنى المقبول ($minimumAcceptableBid ر.س)"
            )
        }

        val updated = lot.copy(
            currentPriceSar = proposedAmountSar,
            highestBidderNumber = bidderNumber,
            totalBidsCount = lot.totalBidsCount + 1,
            status = LotStatus.ACTIVE_BIDDING // Reset hammer countdown on new bid
        )
        lots[index] = updated
        return BidValidationResult(true, updated, null)
    }

    fun advanceHammerCountdown(lotId: String): LotStatus? {
        val index = lots.indexOfFirst { it.lotId == lotId }
        if (index == -1) return null

        val lot = lots[index]
        val nextStatus = when (lot.status) {
            LotStatus.ACTIVE_BIDDING -> LotStatus.GOING_ONCE
            LotStatus.GOING_ONCE -> LotStatus.GOING_TWICE
            LotStatus.GOING_TWICE -> {
                if (lot.currentPriceSar >= lot.reservePriceSar) LotStatus.SOLD else LotStatus.RESERVE_NOT_MET
            }
            else -> lot.status
        }

        lots[index] = lot.copy(status = nextStatus)
        return nextStatus
    }

    fun isReserveMet(lot: AuctionLot): Boolean = lot.currentPriceSar >= lot.reservePriceSar

    fun getTotalAuctionVolumeSar(): Double = lots.sumOf { it.currentPriceSar }
}
