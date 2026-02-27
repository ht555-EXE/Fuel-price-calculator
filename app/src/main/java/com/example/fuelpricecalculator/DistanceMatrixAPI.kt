package com.example.fuelpricecalculator

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

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

interface DistanceMatrixAPI {
    @GET("maps/api/distancematrix/json")
    suspend fun getDistance(
        @Query("origins") origins: String,
        @Query("destinations") destinations: String,
        @Query("key") apiKey: String,
        @Query("units") units: String = "imperial"
    ): DistanceMatrixResponse
}