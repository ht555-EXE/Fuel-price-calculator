package com.example.fuelpricecalculator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first


class FuelRepository(private val tripDao: TripDao, private val carDao: CarDao, private val dvlaApi: DvlaApiService, private val googleApi: DistanceMatrixAPI, val settingsManager: SettingsManager) {
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
                        //TODO: remove support for hybrids and electric
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
    suspend fun calculateAndSaveTrip(license: String, apiKey: String, origin: String, destination: String, originPlaceId: String, destinationPlaceId:String) : Result<Unit>{
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
                    //TODO: Efficiencies in car and car license
                    FuelType.PETROL -> (distanceMiles/car.efficiency) * (settingsManager.petrolPrice.first()/100 *4.54609)
                    FuelType.DIESEL -> (distanceMiles/car.efficiency) * (settingsManager.dieselPrice.first()/100 *4.54609)
                    else -> 0.0
                }
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

    suspend fun updateTrip(trip: Trip){
        tripDao.updateTrip(trip)
    }

    suspend fun updateCar(car: Car){
        carDao.updateCar(car)
    }

    suspend fun clearAllCars(){
        carDao.clearAll()
    }

    suspend fun deleteTrip(trip: Trip){
        tripDao.deleteTrip(trip)
    }

    suspend fun updateSelectedCar(license: String) {
        carDao.updateTimeStamp(license)
    }
}