package com.example.sampleappsplash.model

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sampleappsplash.model.database.BeerDao
import java.io.Serializable


@Entity(tableName = "beers")
data class BeerModel(
    val abv: Double,
    val attenuation_level: Double,
    val brewers_tips: String,
    val contributed_by: String,
    val description: String,
    val ebc: Double,
    val first_brewed: String,
    val ibu: Double,
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "image_url")
    val image_url: String? = null,
    val name: String,
    val ph: Double,
    val srm: Double,
    val tagline: String,
    val target_fg: Double,
    val target_og: Double,
) : Serializable

