package com.example.fuelpricecalculator

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName

interface DvlaApiService {
    @POST("vehicle-enquiry/v1/vehicles")
    suspend fun getVehicleDetails(
        @Header("x-api-key") apiKey: String,
        @Body request: VehicleRequest
    ): Response<VehicleResponse>
}

// Data sent TO the DVLA
data class VehicleRequest(
    @SerializedName("registrationNumber") val registrationNumber: String
)

// Data received FROM the DVLA
data class VehicleResponse(
    val registrationNumber: String,
    val make: String,
    val colour: String,
    val fuelType: String,
    val co2Emissions: Int
)