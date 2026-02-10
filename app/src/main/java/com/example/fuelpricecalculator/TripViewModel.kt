package com.example.fuelpricecalculator

import androidx.lifecycle.*

class TripViewModel(private val repository: TripRepository) : ViewModel() {
    val allTrips: LiveData<List<Trip>> = repository.allTrips.asLiveData()
}