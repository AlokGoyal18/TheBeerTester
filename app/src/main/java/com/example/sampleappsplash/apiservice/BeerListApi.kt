package com.example.sampleappsplash.apiservice

import com.example.sampleappsplash.model.BeerModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BeerListApi {
    private const val BASE_URL = "https://api.punkapi.com/v2/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: BeerListApiService by lazy {
        retrofit.create(BeerListApiService::class.java)
    }
}

class BeerRepository{
    fun getBeers(page:Int, per_page:Int, onSuccess:(ArrayList<BeerModel>)->Unit, onFailure:()->Unit) {
        val call = BeerListApi.apiService.getBeersPerPage(page,per_page)
        call.enqueue(object :Callback<ArrayList<BeerModel>>{
            override fun onResponse(call: Call<ArrayList<BeerModel>>, response: Response<ArrayList<BeerModel>>) {
                if(response.isSuccessful){
                    onSuccess(response.body()!!)
                }
            }

            override fun onFailure(call: Call<ArrayList<BeerModel>>, t: Throwable) {
                onFailure()
            }

        })
    }

    fun getRandomBeer(onSuccess: (ArrayList<BeerModel>) -> Unit, onFailure: () -> Unit){
        val call = BeerListApi.apiService.getRandomBeer()
        call.enqueue(object : Callback<ArrayList<BeerModel>>{
            override fun onResponse(call: Call<ArrayList<BeerModel>>, response: Response<ArrayList<BeerModel>>) {
                if(response.isSuccessful){
                    onSuccess(response.body()!!)
                }
            }

            override fun onFailure(call: Call<ArrayList<BeerModel>>, t: Throwable) {
                onFailure()
            }

        })
    }

    fun searchBeer(keyword:String, onSuccess: (ArrayList<BeerModel>) -> Unit, onFailure: () -> Unit){
        val searchKey= keyword.replace(" ", "_")
        val call = BeerListApi.apiService.searchBeers(searchKey)
        call.enqueue(object : Callback<ArrayList<BeerModel>>{
            override fun onResponse(call: Call<ArrayList<BeerModel>>, response: Response<ArrayList<BeerModel>>) {
                if(response.isSuccessful){
                    onSuccess(response.body()!!)
                }
            }

            override fun onFailure(call: Call<ArrayList<BeerModel>>, t: Throwable) {
                onFailure()
            }

        })
    }
}

