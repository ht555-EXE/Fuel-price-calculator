package com.example.fuelpricecalculator

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ForeignKey

@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(
            entity = Car::class,
            parentColumns = ["license"],
            childColumns = ["carLicense"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["carLicense"])]
)

data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val carLicense: String,
    val destination: String,
    val distance: Double,
    val cost: Double,
    val date: Long
)