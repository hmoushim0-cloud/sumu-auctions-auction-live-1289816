package sa.factory.sumuauctionsauctio.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SumuAuctionsDao {
    @Query("SELECT * FROM auction_live_items_cache ORDER BY id ASC")
    fun observeAllItems(): Flow<List<SumuAuctionsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<SumuAuctionsEntity>)

    @Query("DELETE FROM auction_live_items_cache")
    suspend fun clearAll()
}
