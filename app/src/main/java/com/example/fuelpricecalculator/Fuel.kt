package com.example.fuelpricecalculator

import com.google.gson.annotations.SerializedName

data class FuelResponse(
    val stations: List<Station>,
    @SerializedName("last_updated")
    val lastUpdated: String
)

data class Station(
    @SerializedName("site_id")
    val siteId: String,
    val brand: String,
    val prices: Map<String, Double>
)