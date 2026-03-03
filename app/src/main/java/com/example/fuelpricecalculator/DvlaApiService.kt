package com.example.fuelpricecalculator

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName

/**
 * Retrofit service interface for DVLA Vehicle Enquiry Service (VES).
 *
 */
interface DvlaApiService {
    /**
     * Fetches details for a vehicle based on its registration number.
     *
     * @param apiKey a valid DVLA VES API key
     * @param request a [VehicleRequest] containing the registration number to query.
     * @return a retrofit [Response] wrapping [VehicleResponse] data
     */
    @POST("vehicle-enquiry/v1/vehicles")
    suspend fun getVehicleDetails(
        @Header("x-api-key") apiKey: String,
        @Body request: VehicleRequest
    ): Response<VehicleResponse>
}

/**
 * Data class representing the JSON payload sent to the API
 *
 * @property registrationNumber vehicle registration
 */
data class VehicleRequest(
    @SerializedName("registrationNumber") val registrationNumber: String
)

/**
 * Data class representing the specification returned by the DVLA.
 *
 * @property registrationNumber vehicle registration
 * @property make vehicle manufacturer
 * @property colour vehicle colour
 * @property fuelType vehicle fuel type
 * @property co2Emissions vehicles CO2 g/km rating
 */
data class VehicleResponse(
    val registrationNumber: String,
    val make: String,
    val colour: String,
    val fuelType: String,
    val co2Emissions: Int
)