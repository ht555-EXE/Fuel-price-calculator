package com.example.fuelpricecalculator

data class FuelResponse(
    val stations: List<Station>,
    val lastUpdated: String
)

data class Station(
    val siteId: String,
    val brand: String,
    val prices: Map<String, Double>
)