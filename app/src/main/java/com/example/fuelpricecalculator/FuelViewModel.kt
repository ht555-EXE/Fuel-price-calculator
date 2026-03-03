package com.example.fuelpricecalculator

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * FuelViewModel is responsible for managing UI state,
 * used for bridging UI with [FuelRepository]
 *
 * @property repository the [FuelRepository] instance used by [FuelViewModel]
 */
class FuelViewModel(private val repository: FuelRepository) : ViewModel() {

    val allCars: LiveData<List<Car>> = repository.allCars.asLiveData()
    val currentCarLicense: LiveData<String?> = repository.getCurrentCarLicense().asLiveData()
    val petrolPrice: LiveData<Double> = repository.settingsManager.petrolPrice.asLiveData()
    val dieselPrice: LiveData<Double> = repository.settingsManager.dieselPrice.asLiveData()
    val lastUpdate: LiveData<String> = repository.settingsManager.lastUpdate.asLiveData()
    val useMetric: LiveData<Boolean> = repository.settingsManager.useMetric.asLiveData()

    /**
     * Method for toggling units within [SettingsManager].
     *
     * @param isMetric new measurement unit state, true for metric, false for imperial
     */
    fun toggleUnits(isMetric: Boolean) = viewModelScope.launch{
        repository.settingsManager.saveUnitSystem(isMetric)
    }

    val tripsForCurrentCar: LiveData<List<Trip>> = currentCarLicense.switchMap { license ->
        if (license == null) {
            MutableLiveData(emptyList())
        } else {
            repository.getTripsForCar(license).asLiveData()
        }
    }

    //instances for snack bar prompts
    private val _uiEvent = Channel<UIEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    /**
     * Triggers [DvlaApiService] lookup and saving to Car DB.
     *
     * @param license vehicle license.
     * @param onSuccess callback executed if vehicle is added succcesfully
     */
    fun addNewCar(license: String, onSuccess: () -> Unit){
        viewModelScope.launch {
            //TODO: make secret and reroll
            val apiKey = "wIUdNp8fhYZfbcWT4YdU5rsCvIpJBtx7SHrbvhq1"
            val result = repository.fetchAndSaveVehicle(license, apiKey)
            if(result.isSuccess) {
                _uiEvent.send(UIEvent.ShowSnackBar("Car $license added"))
                onSuccess()
            } else{
                _uiEvent.send(UIEvent.ShowSnackBar(result.exceptionOrNull()?.message ?: "Unknown Error"))
            }
        }
    }

    /**
     * Requests data from [DistanceMatrixAPI] then saves to Trip DB.
     *
     * @param originPlaceId google Places unique ID for origin location
     * @param destinationPlaceId google Places unique ID for destination location
     * @param origin human-readable starting address
     * @param destination human-readable ending address
     */
    fun addNewTrip(originPlaceId: String, destinationPlaceId: String, origin: String, destination: String){
        viewModelScope.launch {
            val license = currentCarLicense.value
            if (license == null) {
                _uiEvent.send(UIEvent.ShowSnackBar("Cannot add trip: No car license selected"))
            }
            //TODO: make api keys local and reroll
            val apiKey = "AIzaSyDOhBfgUByu7EzkbviohlK87YPOiGVYals"
            val result = repository.calculateAndSaveTrip(license, apiKey, origin, destination, originPlaceId, destinationPlaceId)
            if(result.isSuccess){
                _uiEvent.send(UIEvent.ShowSnackBar("Trip added successfully for car: $license"))
            } else{
                _uiEvent.send(UIEvent.ShowSnackBar("Error adding trip: ${result.exceptionOrNull()?.message}"))
            }
        }
    }

    fun selectCar(license: String) {
        viewModelScope.launch {
            repository.updateSelectedCar(license)
        }
    }
    fun deleteCar(car: Car) = viewModelScope.launch {
        repository.deleteCar(car)
    }

    fun deleteTrip(trip: Trip) = viewModelScope.launch {
        repository.deleteTrip(trip)
    }
}

/**
 * Factory class to provide the [FuelRepository] dependency to the [FuelViewModel].
 *
 * @property repository the [FuelRepository] instance used by [FuelViewModel]
 */
class FuelViewModelFactory(private val repository: FuelRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FuelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FuelViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}