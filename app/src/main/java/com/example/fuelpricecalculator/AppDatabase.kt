package com.example.fuelpricecalculator

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Class for allowing global access to database tables for [Trip] and [Car].
 * This also provides access to [TripDao] and [CarDao]
 *
 */
@Database(entities = [Trip::class, Car::class], version = 9, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun carDao(): CarDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Global access method for [AppDatabase].
         *
         * @param context used to locate the database and initialize the [Room] builder
         * @return instance of [AppDatabase]
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fuel_calculator_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}