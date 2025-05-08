package com.example.barmate.repositories

import android.content.Context
import com.example.barmate.data.Drink
import com.example.barmate.data.DrinkDao
import com.example.barmate.data.DrinkDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class Repository(context: Context): DrinkDao {
    private val dao = DrinkDb.getInstance(context).drinkDao()

    // Insert list of drinks
    override suspend fun insertAll(drinks: List<Drink>) = withContext(Dispatchers.IO) {
        dao.insertAll(drinks)
    }

    // Update drink
    override suspend fun updateDrink(drink: Drink) {
        dao.updateDrink(drink)
    }

    // Getters
    override suspend fun getDrinkByName(drinkName: String): Drink = withContext(Dispatchers.IO){
        dao.getDrinkByName(drinkName)
    }

    override fun getAll(): Flow<List<Drink>> {
        return dao.getAll()
    }

    override fun findDrinksByQuery(searchQuery: String): Flow<List<Drink>> {
        return dao.findDrinksByQuery(searchQuery)
    }

    // Delete all drinks from db
    override suspend fun deleteAll() = withContext(Dispatchers.IO) {
        dao.deleteAll()
    }
}