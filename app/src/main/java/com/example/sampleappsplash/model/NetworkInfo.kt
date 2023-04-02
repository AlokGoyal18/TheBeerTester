package com.example.sampleappsplash.model

import android.content.Context
import android.net.ConnectivityManager
import com.example.sampleappsplash.MainActivity

class NetworkInfo(private val context: Context) {
    fun getNetworkInfo():Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return connectivityManager.activeNetworkInfo?.isConnected == true
    }
}