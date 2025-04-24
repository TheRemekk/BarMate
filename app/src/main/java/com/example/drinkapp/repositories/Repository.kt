package com.example.drinkapp.repositories

import android.content.Context
import com.example.drinkapp.data.Drink
import com.example.drinkapp.data.DrinkDao
import com.example.drinkapp.data.DrinkDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class Repository(context: Context): DrinkDao {
    private val dao = DrinkDb.getInstance(context).drinkDao()
    override suspend fun insertAll(drinks: List<Drink>) = withContext(Dispatchers.IO) {
        dao.insertAll(drinks)
    }

    override suspend fun getDrinkByName(drinkName: String): Drink = withContext(Dispatchers.IO){
        dao.getDrinkByName(drinkName)
    }

    override fun getAll(): Flow<List<Drink>> {
        return dao.getAll()
    }

    override suspend fun deleteAll() = withContext(Dispatchers.IO) {
        dao.deleteAll()
    }
}