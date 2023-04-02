package com.example.sampleappsplash.model

import com.example.sampleappsplash.model.database.BeerDao

class BeerDBRepository(private val beerDAO : BeerDao) {
    suspend fun insertBeer(beer : BeerModel) = beerDAO.insert(beer)
    suspend fun insertAllBeer(beerlist : List<BeerModel>) = beerDAO.insertAll(beerlist)
    suspend fun deleteBeer(beer : BeerModel) = beerDAO.delete(beer)
    fun getAllBeers() = beerDAO.getAllBeers()
    fun getBeerById(id : Int) = beerDAO.getBeerById(id)
    fun searchBeers(keyword:String) = beerDAO.searchBeers(keyword)
}