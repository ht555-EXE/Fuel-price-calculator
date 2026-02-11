package com.example.fuelpricecalculator

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class Car (
    @PrimaryKey
    val license: String,
    val model: String?,
    val make: String?,
    val fuelType: FuelType
)