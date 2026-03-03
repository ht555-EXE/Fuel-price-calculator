package com.example.fuelpricecalculator

import com.google.gson.annotations.SerializedName

/**
 * A data class representing fuel price data from BFG API.
 *
 * @property stations a list of [Station] objects representing all BFG stations in the UK
 * @property lastUpdated timestamp provided by the API indicating when data was last synchronized
 */
data class FuelResponse(
    val stations: List<Station>,
    @SerializedName("last_updated")
    val lastUpdated: String
)

/**
 * A data class representing an individual petrol station and its current offerings.
 *
 * @property siteId a unique identifier for the specific station site.
 * @property prices a map where the key is the fuel type (Petrol, Diesel)
 * and the value is the price per litre.
 */
data class Station(
    @SerializedName("site_id")
    val siteId: String,
    val prices: Map<String, Double>
)