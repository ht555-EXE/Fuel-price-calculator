package com.example.fuelpricecalculator

import android.R
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Query("SELECT * FROM cars ORDER BY lastSelected DESC")
    fun getAllCars(): Flow<List<Car>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCar(car: Car)

    @Update
    suspend fun updateCar(car: Car)

    @Query("DELETE FROM cars")
    suspend fun clearAll()

    @Query("UPDATE cars SET lastSelected = :timeStamp WHERE license = :license")
    suspend fun updateTimeStamp(license: String, timeStamp: Long = System.currentTimeMillis())
}