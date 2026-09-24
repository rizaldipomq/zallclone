package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CloneEntity::class, AppSettingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ZallCloneDatabase : RoomDatabase() {
    abstract fun cloneDao(): CloneDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        @Volatile
        private var INSTANCE: ZallCloneDatabase? = null

        fun getDatabase(context: Context): ZallCloneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZallCloneDatabase::class.java,
                    "zall_clone_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
