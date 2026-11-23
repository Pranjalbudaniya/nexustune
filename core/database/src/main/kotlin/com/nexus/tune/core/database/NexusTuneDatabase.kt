package com.nexus.tune.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nexus.tune.core.database.dao.HistoryDao
import com.nexus.tune.core.database.dao.PlaylistDao
import com.nexus.tune.core.database.dao.QueueDao
import com.nexus.tune.core.database.dao.TrackDao
import com.nexus.tune.core.database.entity.*

@Database(
    entities = [
        TrackEntity::class,
        QueueEntity::class,
        HistoryEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NexusTuneDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun queueDao(): QueueDao
    abstract fun historyDao(): HistoryDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: NexusTuneDatabase? = null

        fun getInstance(context: Context): NexusTuneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NexusTuneDatabase::class.java,
                    "nexus_tune.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

