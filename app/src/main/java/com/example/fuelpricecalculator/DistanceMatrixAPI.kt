package com.example.fuelpricecalculator

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Data class representing the response from the Google Maps Distance Matrix API.
 *
 * @property rows a list containing the results for each origin-destination pair
 */
data class DistanceMatrixResponse(
    val rows: List<Row>
) {
    data class Row(val elements: List<Element>)
    data class Element(
        val distance: TextValue?,
        val duration: TextValue?,
        val status: String
    )
    data class TextValue(val text: String, val value: Int)
}

/**
 * Retrofit service interface for Google Maps Distance Matrix API.
 *
 */
interface DistanceMatrixAPI {
    /**
     * Gets the calculated result for the travel time and distance between two points.
     *
     * @param origins The starting point address/city
     * @param destinations the ending point address/city
     * @param apiKey a valid Google Maps API key
     * @param units the unit system used for calculation
     * @return a [DistanceMatrixResponse] containing the returned trip data
     */
    @GET("maps/api/distancematrix/json")
    suspend fun getDistance(
        @Query("origins") origins: String,
        @Query("destinations") destinations: String,
        @Query("key") apiKey: String,
        @Query("units") units: String = "imperial"
    ): DistanceMatrixResponse
}