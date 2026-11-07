package sa.factory.sumuauctionsauctio.data.repository

import sa.factory.sumuauctionsauctio.domain.repository.SumuAuctionsRepository
import sa.factory.sumuauctionsauctio.hardware.HardwareSensorDriver
import sa.factory.sumuauctionsauctio.model.SumuAuctionsItem
import sa.factory.sumuauctionsauctio.model.SumuAuctionsOrder
import sa.factory.sumuauctionsauctio.model.SumuAuctionsTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SumuAuctionsRepositoryImpl(
    private val sensorDriver: HardwareSensorDriver
) : SumuAuctionsRepository {

    private val _catalog = MutableStateFlow<List<SumuAuctionsItem>>(listOf(
        SumuAuctionsItem("1", "الباقة التنفيذية المعتمدة", "تكامل مباشر مع BLE Hardware Bidding Paddle GATT + Sub-100ms WebRTC Audio", 180.0, "معتمد", "الخدمات الرئيسية"),
        SumuAuctionsItem("2", "خدمة المعالجة الفورية", "أداء أصيل بسرعة 60 إطار في الثانية", 95.0, "فوري", "المراقبة الحية"),
        SumuAuctionsItem("3", "باقة الصيانة والدعم السحابي", "تشفير كامل للبيانات المحلية ومزامنة آمنة", 250.0, "شامل", "التقارير الضريبية")
    ))
    override val catalogFlow: Flow<List<SumuAuctionsItem>> = _catalog.asStateFlow()

    private val _cart = MutableStateFlow<List<SumuAuctionsItem>>(emptyList())
    override val activeCart: StateFlow<List<SumuAuctionsItem>> = _cart.asStateFlow()

    override val telemetryFlow: Flow<SumuAuctionsTelemetry> = sensorDriver.observePeripheralStream()
        .map { frame ->
            SumuAuctionsTelemetry(
                peripheralName = frame.peripheralName,
                status = frame.connectionState,
                liveReading = frame.livePayload,
                batteryPct = 96,
                signalDbm = -54
            )
        }

    override fun addToCart(item: SumuAuctionsItem) {
        val current = _cart.value.toMutableList()
        current.add(item)
        _cart.value = current
    }

    override fun removeFromCart(item: SumuAuctionsItem) {
        val current = _cart.value.toMutableList()
        current.remove(item)
        _cart.value = current
    }

    override fun clearCart() {
        _cart.value = emptyList()
    }

    override suspend fun checkout(paymentMethod: String): SumuAuctionsOrder {
        val currentItems = _cart.value.toList()
        val subtotal = currentItems.sumOf { it.priceSar }
        val vat = subtotal * 0.15
        val total = subtotal + vat
        val order = SumuAuctionsOrder(
            orderId = "ORD-${System.currentTimeMillis() % 100000}",
            items = currentItems,
            subtotalSar = subtotal,
            vatSar = vat,
            totalSar = total,
            paymentMethod = paymentMethod
        )
        clearCart()
        return order
    }

    override suspend fun refreshCatalog() {
        // Offline-first sync logic
    }
}
