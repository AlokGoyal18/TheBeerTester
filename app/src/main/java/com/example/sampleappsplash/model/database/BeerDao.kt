package com.example.sampleappsplash.model.database


import com.example.sampleappsplash.model.BeerModel
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BeerDao {

    @Query("SELECT * FROM beers")
    fun getAllBeers():List<BeerModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(beers: List<BeerModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(beer: BeerModel)

    @Query("SELECT * FROM beers WHERE id= :id")
    fun getBeerById(id:Int):List<BeerModel>

    @Query("SELECT * FROM beers WHERE name LIKE '%' || :keyword || '%'")
    fun searchBeers(keyword:String):List<BeerModel>

    @Delete
    fun delete(item : BeerModel)
}