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
            childColumns = ["license"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["license"])]
)

data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val license: String?,
    val origin: String,
    val destination: String,
    val distance: Double,
    val duration: String,
    val cost: Double,
    val date: Long
)