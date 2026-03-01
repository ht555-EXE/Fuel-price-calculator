package com.example.fuelpricecalculator

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class FuelViewModel(private val repository: FuelRepository) : ViewModel() {

    val allCars: LiveData<List<Car>> = repository.allCars.asLiveData()

    val currentCarLicense: LiveData<String?> = repository.getCurrentCarLicense().asLiveData()

    val tripsForCurrentCar: LiveData<List<Trip>> = currentCarLicense.switchMap { license ->
        if (license == null) {
            MutableLiveData(emptyList())
        } else {
            repository.getTripsForCar(license).asLiveData()
        }
    }

    fun addNewCar(license: String){
        viewModelScope.launch {
            val apiKey = "wIUdNp8fhYZfbcWT4YdU5rsCvIpJBtx7SHrbvhq1"
            val result = repository.fetchAndSaveVehicle(license, apiKey)
            if(result.isSuccess) {
                Log.d("FuelViewModel", "Car added successfully: $license")
            } else{
                Log.e("FuelViewModel", "Error adding car: ${result.exceptionOrNull()?.message}")
            }
        }
    }
    
    fun addNewTrip(originPlaceId: String, destinationPlaceId: String, origin: String, destination: String){
        val license = currentCarLicense.value
        if (license == null) {
            Log.e("FuelViewModel", "Cannot add trip: No car license selected!")
            return
        }

        viewModelScope.launch {
            //TODO: make api keys local and reroll
            val apiKey = "AIzaSyDOhBfgUByu7EzkbviohlK87YPOiGVYals"
            val result = repository.calculateAndSaveTrip(license, apiKey, origin, destination, originPlaceId, destinationPlaceId)
            if(result.isSuccess){
                Log.d("FuelViewModel", "Trip added successfully for car: $license")
            } else{
                Log.e("FuelViewModel", "Error adding trip: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun selectCar(license: String) {
        viewModelScope.launch {
            repository.updateSelectedCar(license)
        }
    }

    fun insertTrip(trip: Trip) = viewModelScope.launch {
        repository.insertTrip(trip)
    }

    fun updateTrip(trip: Trip) = viewModelScope.launch {
        repository.updateTrip(trip)
    }

    fun deleteTrip(trip: Trip) = viewModelScope.launch {
        repository.deleteTrip(trip)
    }
}

class FuelViewModelFactory(private val repository: FuelRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FuelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FuelViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}