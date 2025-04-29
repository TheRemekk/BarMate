package com.example.barmate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Drink::class], version = 3)
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
            )
                .addMigrations(migration_1_2, migration_2_3)
                .fallbackToDestructiveMigration()
                .build()
        }
        return db!!
    }

    val migration_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE drinks ADD COLUMN imageResId INTEGER DEFAULT 0 NOT NULL")
        }
    }

    val migration_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE drinks ADD COLUMN isFavourite INTEGER DEFAULT 0 NOT NULL")
        }
    }
}