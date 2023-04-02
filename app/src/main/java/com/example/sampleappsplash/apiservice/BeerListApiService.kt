package com.example.sampleappsplash.apiservice

import com.example.sampleappsplash.model.BeerModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface BeerListApiService {
    @Headers("Content-Type: application/json")
    @GET("beers")
    fun getBeersPerPage(
        @Query("page")page:Int,
        @Query("per_page")per_page:Int
    ): Call<ArrayList<BeerModel>>

    @Headers("Content-Type: application/json")
    @GET("beers/random")
    fun getRandomBeer(): Call<ArrayList<BeerModel>>

    @Headers("Content-Type: application/json")
    @GET("beers")
    fun searchBeers(
        @Query("beer_name")beer_name:String
    ): Call<ArrayList<BeerModel>>
}