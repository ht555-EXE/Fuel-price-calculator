package com.example.fuelpricecalculator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

/**
 * Central repository managing coordination between local storage and remote API data services.
 *
 * @property tripDao data access object for performing [Trip] database operations
 * @property carDao data access object for performing [Car] database operations
 * @property dvlaApi retrofit service for querying official UK vehicle technical specifications
 * @property googleApi retrofit service for calculating travel distances and durations
 * @property settingsManager handler for persistent user preferences and cached fuel prices
 */
class FuelRepository(private val tripDao: TripDao, private val carDao: CarDao, private val dvlaApi: DvlaApiService, private val googleApi: DistanceMatrixAPI, val settingsManager: SettingsManager) {
    val allCars: Flow<List<Car>> = carDao.getAllCars()

    /**
     * Fetches vehicle specifications from the DVLA through [DvlaApiService],
     * saving the result to the vehicle database.
     *
     * @param registration vehicle registration
     * @param apiKey valid DVLA API key
     * @return a [Result] indicating success
     */
    suspend fun fetchAndSaveVehicle(registration: String, apiKey: String): Result<Unit> {
        return try {
            val existingCar = carDao.getCarByLicense(registration).firstOrNull()
            if (existingCar != null){
                return Result.failure(Exception("Vehicle $registration is already saved"))
            }
            val response = dvlaApi.getVehicleDetails(apiKey, VehicleRequest(registration))
            //response checking
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                if(data.fuelType != "PETROL" && data.fuelType != "DIESEL"){
                    return Result.failure(Exception("Sorry, only Petrol and Diesel vehicles are supported"))
                }
                //no C02 emissions data means no efficiency calculation
                if(data.co2Emissions == 0){
                    return Result.failure(Exception("Insufficient information on vehicle"))
                }
                val newCar = Car(
                    license = data.registrationNumber,
                    colour = data.colour,
                    make = data.make,
                    fuelType = when (data.fuelType) {
                        "PETROL" -> FuelType.PETROL
                        "DIESEL" -> FuelType.DIESEL
                        //app does not currently support electric or hybrid vehicles
                        else -> return Result.failure(Exception("Sorry, only Petrol and Diesel vehicles are supported"))
                    },
                    efficiency = when (data.fuelType) {
                        "PETROL" -> 8887/ (data.co2Emissions * 1.60934)
                        "DIESEL" -> 10180/ (data.co2Emissions * 1.60934)
                        else -> 0.0
                    }
                )

                insertCar(newCar)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Vehicle not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calculates trip metrics using [DistanceMatrixAPI],
     * creating a persistent trip history.
     *
     * @param license registration of car used during trip
     * @param apiKey valid Google Maps API key
     * @param origin human-readable starting address
     * @param destination human-readable ending address
     * @param originPlaceId google Places unique ID for origin location
     * @param destinationPlaceId google Places unique ID for origin location
     * @return a [Result] indicating success
     */
    suspend fun calculateAndSaveTrip(license: String?, apiKey: String, origin: String,
                                     destination: String, originPlaceId: String,
                                     destinationPlaceId: String) : Result<Unit>{
        return try {
            val car = carDao.getCarByLicense(license).first()
            val response = googleApi.getDistance(
                origins = "place_id:$originPlaceId",
                destinations = "place_id:$destinationPlaceId",
                apiKey = apiKey
            )
            val element = response.rows.firstOrNull()?.elements?.firstOrNull()
            if(element?.status == "OK"){
                val distanceMiles = (element.distance?.value ?: 0) / 1609.344
                val duration = element.duration?.text ?: ""
                val cost = when (car?.fuelType){
                    //cost based off efficiency, distance and fuel cost.
                    FuelType.PETROL -> (distanceMiles/car.efficiency) * (settingsManager.petrolPrice.first()/100 *4.54609)
                    FuelType.DIESEL -> (distanceMiles/car.efficiency) * (settingsManager.dieselPrice.first()/100 *4.54609)
                    else -> 0.0
                }
                //saving to DB
                val trip = Trip(
                    license = license,
                    origin = origin,
                    destination = destination,
                    distance = distanceMiles,
                    duration = duration,
                    cost = cost,
                    date = System.currentTimeMillis()
                )
                insertTrip(trip)
                Result.success(Unit)
            } else{
                Result.failure(Exception("Could not calculate distance: ${element?.status}"))
            }
        } catch (e: Exception){
            Result.failure(e)
        }

    }
    fun getTripsForCar(registration: String): Flow<List<Trip>>{
        return tripDao.getTripsByCar(registration)
    }

     fun getCurrentCarLicense() : Flow<String?> {
        return carDao.getCurrentCarLicense()
    }

    suspend fun insertCar(car: Car){
        carDao.insertCar(car)
    }

    suspend fun insertTrip(trip: Trip){
        tripDao.insertTrip(trip)
    }

    suspend fun deleteCar(car: Car){
        carDao.deleteCar(car)
    }

    suspend fun deleteTrip(trip: Trip){
        tripDao.deleteTrip(trip)
    }

    suspend fun updateSelectedCar(license: String) {
        carDao.updateTimeStamp(license)
    }
}