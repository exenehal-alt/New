package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ManuscriptEntity::class], version = 1, exportSchema = false)
abstract class PandulipiDatabase : RoomDatabase() {
    abstract fun manuscriptDao(): ManuscriptDao

    companion object {
        @Volatile
        private var INSTANCE: PandulipiDatabase? = null

        fun getInstance(context: Context): PandulipiDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PandulipiDatabase::class.java,
                    "pandulipi_manuscripts.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
