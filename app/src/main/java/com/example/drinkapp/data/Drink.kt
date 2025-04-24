package com.example.drinkapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drinks")
data class Drink(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val name: String,
    val ingredients: String,
    val desc: String,
    val imageResId: Int,
    val shakingTime: Int
)
