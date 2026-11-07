package sa.factory.sumuauctionsauctio.domain.usecase

import sa.factory.sumuauctionsauctio.domain.repository.SumuAuctionsRepository
import sa.factory.sumuauctionsauctio.model.SumuAuctionsItem
import sa.factory.sumuauctionsauctio.model.SumuAuctionsOrder
import sa.factory.sumuauctionsauctio.model.SumuAuctionsTelemetry
import kotlinx.coroutines.flow.Flow

class GetCatalogUseCase(private val repository: SumuAuctionsRepository) {
    operator fun invoke(): Flow<List<SumuAuctionsItem>> = repository.catalogFlow
}

class ManageCartUseCase(private val repository: SumuAuctionsRepository) {
    val cart = repository.activeCart
    fun add(item: SumuAuctionsItem) = repository.addToCart(item)
    fun remove(item: SumuAuctionsItem) = repository.removeFromCart(item)
    fun clear() = repository.clearCart()
}

class ProcessCheckoutUseCase(private val repository: SumuAuctionsRepository) {
    suspend operator fun invoke(paymentMethod: String = "Mada Contactless EMV"): SumuAuctionsOrder =
        repository.checkout(paymentMethod)
}

class ObserveHardwareTelemetryUseCase(private val repository: SumuAuctionsRepository) {
    operator fun invoke(): Flow<SumuAuctionsTelemetry> = repository.telemetryFlow
}
