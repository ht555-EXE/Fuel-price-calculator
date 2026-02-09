package com.example.fuelpricecalculator

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Trip::class], version = 1, exportSchema = true)
abstract class TripDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: TripDatabase? = null

        fun getDatabase(context: Context): TripDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder<TripDatabase>(
                    context.applicationContext,
                    TripDatabase::class.java,
                    "trip_database"

                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}