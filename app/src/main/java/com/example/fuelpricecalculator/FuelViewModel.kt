package com.example.fuelpricecalculator

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class FuelViewModel(private val repository: FuelRepository) : ViewModel() {

    val allCars: LiveData<List<Car>> = repository.allCars.asLiveData()
    val allTrips: LiveData<List<Trip>> = repository.allTrips.asLiveData()

    private val selectedCarLicense = MutableLiveData<String?>()

    val tripsForSelectedCar: LiveData<List<Trip>> = selectedCarLicense.switchMap { license ->
        if (license == null) {
            MutableLiveData(emptyList())
        } else {
            repository.getTripsForCar(license).asLiveData()
        }
    }

    fun selectCar(license: String) {
        selectedCarLicense.value = license
    }

    fun insertCar(car: Car) = viewModelScope.launch {
        repository.insertCar(car)
    }

    fun insertTrip(trip: Trip) = viewModelScope.launch {
        repository.insertTrip(trip)
    }

    fun updateTrip(trip: Trip) = viewModelScope.launch {
        repository.updateTrip(trip)
    }

    fun updateCar(car: Car) = viewModelScope.launch {
        repository.updateCar(car)
    }

    fun clearAllTrips() = viewModelScope.launch {
        repository.clearAllTrips()
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