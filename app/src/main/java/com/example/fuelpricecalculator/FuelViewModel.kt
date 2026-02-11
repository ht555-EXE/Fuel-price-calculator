package com.example.fuelpricecalculator

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class FuelViewModel(private val repository: FuelRepository) : ViewModel() {

    val allCars: LiveData<List<Car>> = repository.allCars.asLiveData()
    val allTrips: LiveData<List<Trip>> = repository.allTrips.asLiveData()

    private val _selectedCarLicense = MutableLiveData<String?>()

    val tripsForSelectedCar: LiveData<List<Trip>> = _selectedCarLicense.switchMap { license ->
        if (license == null) {
            MutableLiveData(emptyList())
        } else {
            repository.getTripsForCar(license).asLiveData()
        }
    }

    fun selectCar(license: String) {
        _selectedCarLicense.value = license
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

    fun clearAllTrips() = viewModelScope.launch {
        repository.clearAllTrips()
    }
}