package com.example.fuelpricecalculator

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class for Car properties that are required for saving to the database.
 *
 * @property license
 * @property colour
 * @property make
 * @property fuelType
 * @property lastSelected
 * @property efficiency
 */
@Entity(tableName = "cars")
data class Car (
    @PrimaryKey
    val license: String,
    val colour: String,
    val make: String,
    val fuelType: FuelType,
    val lastSelected: Long = System.currentTimeMillis(),
    val efficiency: Double
)