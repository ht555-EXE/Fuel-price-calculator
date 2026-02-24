package com.example.fuelpricecalculator

import kotlinx.coroutines.flow.Flow

class FuelRepository(private val tripDao: TripDao, private val carDao: CarDao, private val dvlaApi: DvlaApiService) {
    val allCars: Flow<List<Car>> = carDao.getAllCars()
    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()

    suspend fun fetchAndSaveVehicle(registration: String, apiKey: String): Result<Unit> {
        return try {
            val response = dvlaApi.getVehicleDetails(apiKey, VehicleRequest(registration))

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val newCar = Car(
                    license = data.registrationNumber,
                    colour = data.colour,
                    make = data.make,
                    fuelType = when (data.fuelType.uppercase()) {
                        "PETROL" -> FuelType.PETROL
                        "DIESEL" -> FuelType.DIESEL
                        "ELECTRICITY" -> FuelType.ELECTRIC
                        "HYBRID ELECTRIC" -> FuelType.HYBRID
                        else -> FuelType.UNKNOWN
                    },
                    efficiency = when (data.fuelType.uppercase()) {
                        "PETROL" -> 8887/ (data.co2Emissions * 1.60934)
                        "DIESEL" -> 10180/ (data.co2Emissions * 1.60934)
                        else -> 0.0
                    }
                )

                insertCar(newCar)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Vehicle not found or API error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
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

    suspend fun updateSelectedCar(license: String) {
        carDao.updateTimeStamp(license)
    }
}