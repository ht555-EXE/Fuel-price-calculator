package com.example.fuelpricecalculator

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Query("SELECT * FROM cars ORDER BY lastSelected DESC")
    fun getAllCars(): Flow<List<Car>>

    @Query("SELECT license FROM cars ORDER BY lastSelected DESC LIMIT 1")
    fun getCurrentCarLicense(): Flow<String?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCar(car: Car)

    @Delete
    suspend fun deleteCar(car: Car)

    @Query("UPDATE cars SET lastSelected = :timeStamp WHERE license = :license")
    suspend fun updateTimeStamp(license: String, timeStamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM cars WHERE license = :license LIMIT 1")
    fun getCarByLicense(license: String?): Flow<Car?>
}