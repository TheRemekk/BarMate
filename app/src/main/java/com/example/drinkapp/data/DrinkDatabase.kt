package com.example.drinkapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Drink::class], version = 2)
abstract class DrinkDatabase : RoomDatabase() {
    abstract fun drinkDao(): DrinkDao
}

object DrinkDb {
    private var db: DrinkDatabase? = null

    fun getInstance(context: Context): DrinkDatabase {
        if(db == null) {
            db = Room.databaseBuilder(
                context,
                DrinkDatabase::class.java,
                "drink-database"
            ).addMigrations(migration_1_2)
                .build()
        }
        return db!!
    }

    val migration_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE drinks ADD COLUMN imageResId INTEGER DEFAULT 0 NOT NULL")
        }
    }
}