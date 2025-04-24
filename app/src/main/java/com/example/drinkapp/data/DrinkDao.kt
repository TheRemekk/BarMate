package com.example.drinkapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkDao {
    @Insert
    suspend fun insertAll(drinks: List<Drink>)

    @Query("SELECT * FROM drinks WHERE drinks.name = :drinkName")
    suspend fun getDrinkByName(drinkName: String): Drink

    @Query("SELECT * FROM drinks")
    fun getAll(): Flow<List<Drink>>

    @Query("DELETE FROM drinks")
    suspend fun deleteAll()

}