package com.example.sampleappsplash

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sampleappsplash.apiservice.BeerRepository
import com.example.sampleappsplash.model.BeerModel
import com.example.sampleappsplash.model.BeerViewType
import com.example.sampleappsplash.model.NetworkInfo
import com.example.sampleappsplash.view.BeerList
import com.example.sampleappsplash.view.BeerViewActivity
import com.example.sampleappsplash.view.SearchActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<LinearLayout>(R.id.BeerButton)
        val savedListButton = findViewById<LinearLayout>(R.id.SavedBeerButton)
        val randomButton = findViewById<LinearLayout>(R.id.RandomBeerButton)
        val searchButton = findViewById<LinearLayout>(R.id.SearchBeerButton)

        button.setOnClickListener {
            val intent = Intent(this@MainActivity,BeerList::class.java)
            intent.putExtra("isSaved",BeerViewType.ONLINE.name)
            supportActionBar?.hide()
            startActivity(intent)
        }

        savedListButton.setOnClickListener{
            val intent = Intent(this@MainActivity,BeerList::class.java)
            intent.putExtra("isSaved",BeerViewType.SAVED.name)
            supportActionBar?.hide()
            startActivity(intent)
        }

        randomButton.setOnClickListener {
            if(NetworkInfo(this@MainActivity).getNetworkInfo()) {
                CoroutineScope(Dispatchers.Default).launch {
                    BeerRepository().getRandomBeer(
                        onSuccess = {
                            openRandomBeer(it.first())
                        },
                        onFailure = {
                            Toast.makeText(this@MainActivity,getString(R.string.basic_error),Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            else{
                Toast.makeText(this@MainActivity,getString(R.string.no_internet),Toast.LENGTH_SHORT).show()
            }
        }

        searchButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SearchActivity::class.java)
            supportActionBar?.hide()
            startActivity(intent)
        }
    }

    private fun openRandomBeer(beer:BeerModel){
        CoroutineScope(Dispatchers.Main).launch{
            val intent = Intent(this@MainActivity, BeerViewActivity::class.java)
            intent.putExtra("BeerModel", beer)
            supportActionBar?.hide()
            startActivity(intent)
        }
    }


}