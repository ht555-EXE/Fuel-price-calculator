package com.example.fuelpricecalculator

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class FuelRepository(private val tripDao: TripDao, private val carDao: CarDao) {
    val allCars: Flow<List<Car>> = carDao.getAllCars()
    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()

    fun getTripsForCar(registration: String): Flow<List<Trip>>{
        return tripDao.getTripsByCar(registration)
    }

    suspend fun insertCar(car: Car){
        carDao.insertCar(car)
    }

    suspend fun insertTrip(trip: Trip){
        tripDao.insertTrip(trip)
    }

    suspend fun updateTrip(trip: Trip){
        tripDao.updateTrip(trip)
    }

    suspend fun updateCar(car: Car){
        carDao.updateCar(car)
    }

    suspend fun clearAllTrips(){
        tripDao.clearAll()
    }

    suspend fun clearAllCars(){
        carDao.clearAll()
    }

}