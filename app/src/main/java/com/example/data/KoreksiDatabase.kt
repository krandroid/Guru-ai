package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AnswerKey::class, GradingHistory::class], version = 1, exportSchema = false)
abstract class KoreksiDatabase : RoomDatabase() {
    abstract fun koreksiDao(): KoreksiDao

    companion object {
        @Volatile
        private var INSTANCE: KoreksiDatabase? = null

        fun getDatabase(context: Context): KoreksiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KoreksiDatabase::class.java,
                    "koreksi_otomatis_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
