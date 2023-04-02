package com.example.sampleappsplash.viewmodel


import com.example.sampleappsplash.model.BeerModel
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sampleappsplash.apiservice.BeerRepository
import com.example.sampleappsplash.model.BeerDBRepository
import com.example.sampleappsplash.model.BeerDatabase
import com.example.sampleappsplash.model.BeerViewType
import com.example.sampleappsplash.model.NetworkInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception

class BeerListViewModel: ViewModel() {
    val beerRepository = BeerRepository()
    val beerList :MutableLiveData<MutableList<BeerModel>> = MutableLiveData()
    val errorThrow:MutableLiveData<Boolean> = MutableLiveData()
    private var dbDone = false
    private var apiDone = false


    fun loadBeer(page:Int,per_page:Int, isSaved:BeerViewType,context: Context, cm:ConnectivityManager){
        viewModelScope.launch {
            try {
                if(isSaved == BeerViewType.ONLINE) {
                    if(cm.activeNetworkInfo?.isConnected == true) {
                        beerRepository.getBeers(page, per_page, onSuccess = {
                            beerList.value = it.toMutableList()
                        },
                            onFailure = {
                                errorThrow.value = errorThrow.value != true
                            })
                    }
                }
                else{
                    CoroutineScope(Dispatchers.Default).launch {
                        try {
                            val tempData = BeerDBRepository(
                                BeerDatabase.getInstance(context.applicationContext).beerDao()
                            ).getAllBeers().toMutableList()
                            passValueToBeerList(tempData)
                        }catch(e:Exception){
                            errorThrow.value = errorThrow.value != true
                        }
                    }
                }

            }
            catch(e:Exception){
                e.printStackTrace()
                errorThrow.value = true
            }
        }

    }

    fun searchBeers(keyword: String, context: Context, cm:ConnectivityManager){
        viewModelScope.launch {
            val tempList= arrayListOf<BeerModel>()
            try{
                CoroutineScope(Dispatchers.Default).launch{
                    val dbList = BeerDBRepository(
                        BeerDatabase.getInstance(context.applicationContext).beerDao()
                    ).searchBeers(keyword).toList()
                    if(dbList.isNotEmpty()) {
                        tempList.addAll(dbList)
                        tempList.distinct()
                        dbDone = true
                        if (dbDone && apiDone) {
                            passValueToListOnCompletion(tempList)
                        }
                    }else{
                        dbDone = true
                        if(dbDone && apiDone){
                            if(tempList.isEmpty()) {
                                dbDone = false
                                apiDone = false
                                errorValueSet()
                            }else{
                                passValueToListOnCompletion(tempList)
                            }
                        }
                    }
                }
                async {
                    if (cm.activeNetworkInfo?.isConnected == true) {
                        beerRepository.searchBeer(keyword = keyword,
                            onSuccess = {
                                if(it.isNotEmpty()) {
                                    tempList.addAll(it)
                                    tempList.distinct()
                                    apiDone = true
                                    if (dbDone && apiDone) {
                                        passValueToListOnCompletion(tempList)
                                    }
                                }else{
                                    apiDone = true
                                    if(dbDone && apiDone){
                                        if(tempList.isEmpty()) {
                                            dbDone = false
                                            apiDone = false
                                            errorThrow.value = errorThrow.value != true
                                        }else{
                                            passValueToListOnCompletion(tempList)
                                        }
                                    }
                                }
                            },
                            onFailure = {
                                apiDone = true
                                if(dbDone && apiDone){
                                    passValueToListOnCompletion(tempList)
                                }
                                errorThrow.value = errorThrow.value != true
                            })
                    }
                }
            }catch(e:Exception){
                e.printStackTrace()
                 passValueToBeerList(tempList)
                errorThrow.value = errorThrow.value != true
            }
        }
    }

    private fun passValueToBeerList(list:MutableList<BeerModel>){
        viewModelScope.launch {
            beerList.value = list
        }
    }
    private fun passValueToListOnCompletion(list: MutableList<BeerModel>){
        viewModelScope.launch {
            if(dbDone && apiDone) {
                dbDone = false
                apiDone = false
                beerList.value = list
            }
        }
    }

    private fun errorValueSet(){
        viewModelScope.launch {
            errorThrow.value = errorThrow.value != true
        }
    }
}