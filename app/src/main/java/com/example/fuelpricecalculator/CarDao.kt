package com.example.fuelpricecalculator

import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

interface CarDao {
    @Query("SELECT * FROM cars")
    fun getAllCars(): Flow<List<Car>>

    @Insert
    suspend fun insertCar(car: Car)

    @Update
    suspend fun updateCar(car: Car)

    @Query("DELETE FROM cars")
    suspend fun clearAll()
}