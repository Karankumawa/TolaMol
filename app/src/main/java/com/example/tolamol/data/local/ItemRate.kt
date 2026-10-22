package com.example.tolamol.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item_rates")
data class ItemRate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val pricePerKg: Double
)
