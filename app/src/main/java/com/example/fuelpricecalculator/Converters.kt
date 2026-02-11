package com.example.fuelpricecalculator

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromFuelType(fuel: FuelType?): String?{
        return fuel?.name
    }

    @TypeConverter
    fun toFuelType(value: String?): FuelType {
        if (value.isNullOrBlank()) {
            return FuelType.PETROL
        }
        return try {
            FuelType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            FuelType.PETROL
        }
    }
}