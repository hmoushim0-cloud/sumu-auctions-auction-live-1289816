package sa.factory.sumuauctionsauctio.domain.repository

import sa.factory.sumuauctionsauctio.model.SumuAuctionsItem
import sa.factory.sumuauctionsauctio.model.SumuAuctionsOrder
import sa.factory.sumuauctionsauctio.model.SumuAuctionsTelemetry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SumuAuctionsRepository {
    val catalogFlow: Flow<List<SumuAuctionsItem>>
    val activeCart: StateFlow<List<SumuAuctionsItem>>
    val telemetryFlow: Flow<SumuAuctionsTelemetry>
    
    fun addToCart(item: SumuAuctionsItem)
    fun removeFromCart(item: SumuAuctionsItem)
    fun clearCart()
    suspend fun checkout(paymentMethod: String): SumuAuctionsOrder
    suspend fun refreshCatalog()
}
