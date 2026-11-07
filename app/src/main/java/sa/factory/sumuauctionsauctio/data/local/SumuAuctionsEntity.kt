package sa.factory.sumuauctionsauctio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import sa.factory.sumuauctionsauctio.model.SumuAuctionsItem

@Entity(tableName = "auction_live_items_cache")
data class SumuAuctionsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val priceSar: Double,
    val badge: String,
    val category: String,
    val status: String
) {
    fun toDomain(): SumuAuctionsItem = SumuAuctionsItem(
        id = id,
        title = title,
        description = description,
        priceSar = priceSar,
        badge = badge,
        category = category,
        status = status
    )
}
