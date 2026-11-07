package sa.factory.sumuauctionsauctio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SumuAuctionsEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun auction_liveDao(): SumuAuctionsDao
}
