package com.example.fuelpricecalculator

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class TripRepository(private val tripDao: TripDao) {

    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()
    @WorkerThread
    fun getAllTrips(){
        tripDao.getAllTrips()
    }

    @WorkerThread
    suspend fun insertTrip(tripItem: Trip){
        tripDao.insertTrip(tripItem)
    }

    @WorkerThread
    suspend fun updateTrip(tripItem: Trip){
        tripDao.updateTrip(tripItem)
    }

    @WorkerThread
    suspend fun clearAllTrips(){
        tripDao.clearAll()
    }

}