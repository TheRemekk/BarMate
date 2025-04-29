package com.example.barmate.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkDao {
    @Insert
    suspend fun insertAll(drinks: List<Drink>)

    @Update
    suspend fun updateDrink(drink: Drink)

    @Query("SELECT * FROM drinks WHERE drinks.name = :drinkName")
    suspend fun getDrinkByName(drinkName: String): Drink

    @Query("SELECT * FROM drinks WHERE drinks.isFavourite = :isFavourite")
    fun getFavDrinksByName(isFavourite: Int): Flow<List<Drink>>

    @Query("SELECT * FROM drinks WHERE name LIKE :searchQuery")
    fun findDrinksByQuery(searchQuery: String): Flow<List<Drink>>

    @Query("SELECT * FROM drinks")
    fun getAll(): Flow<List<Drink>>

    @Query("DELETE FROM drinks")
    suspend fun deleteAll()

}