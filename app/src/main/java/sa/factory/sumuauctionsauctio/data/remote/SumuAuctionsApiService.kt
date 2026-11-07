package sa.factory.sumuauctionsauctio.data.remote

import sa.factory.sumuauctionsauctio.model.SumuAuctionsItem
import sa.factory.sumuauctionsauctio.model.SumuAuctionsOrder

interface SumuAuctionsApiService {
    suspend fun fetchCatalog(): List<SumuAuctionsItem>
    suspend fun submitOrder(order: SumuAuctionsOrder): Boolean
}
