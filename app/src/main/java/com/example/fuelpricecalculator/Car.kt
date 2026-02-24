package com.example.fuelpricecalculator

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class Car (
    @PrimaryKey
    val license: String,
    val colour: String,
    val make: String?,
    val fuelType: FuelType,
    val lastSelected: Long = System.currentTimeMillis(),
    val efficiency: Double?
)