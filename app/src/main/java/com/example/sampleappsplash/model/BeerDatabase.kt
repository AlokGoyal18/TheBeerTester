package com.example.sampleappsplash.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sampleappsplash.model.database.BeerDao


@Database(entities = [BeerModel::class], version = 1)
abstract class BeerDatabase : RoomDatabase() {
    abstract fun beerDao(): BeerDao

    companion object {
        private const val DB_NAME = "beer_app_database"

        @Volatile
        private var instances: BeerDatabase? = null

        fun getInstance(context: Context): BeerDatabase {
            val tempInstance = instances
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BeerDatabase::class.java,
                    DB_NAME
                ).build()
                instances = instance
                return instances as BeerDatabase
            }
        }
    }
}