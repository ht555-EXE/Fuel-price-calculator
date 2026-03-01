package com.example.fuelpricecalculator

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Query("SELECT * FROM trips WHERE license = :registration ORDER BY date DESC")
    fun getTripsByCar(registration: String): Flow<List<Trip>>

    @Query("SELECT * FROM trips")
    fun getAllTrips(): Flow<List<Trip>>

    @Insert
    suspend fun insertTrip(trip: Trip)

    @Update
    suspend fun updateTrip(trip: Trip)
    @Delete
    suspend fun deleteTrip(trip: Trip)
}