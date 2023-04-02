package com.example.sampleappsplash.view

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sampleappsplash.R
import com.example.sampleappsplash.model.BeerModel
import com.example.sampleappsplash.model.BeerViewType
import com.example.sampleappsplash.viewmodel.BeerListViewModel

class SearchActivity : AppCompatActivity() {
    private lateinit var viewModel: BeerListViewModel
    private lateinit var listView: RecyclerView
    private lateinit var searchText: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: BeerListAdapter
    private lateinit var cm : ConnectivityManager
    private lateinit var context: Context
    var isLoading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        cm = this@SearchActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        viewModel = ViewModelProvider(this).get(BeerListViewModel::class.java)
        listView = findViewById(R.id.listView)
        searchText = findViewById(R.id.searchText)
        progressBar = findViewById(R.id.progress_search)
        val layoutManager = LinearLayoutManager(this@SearchActivity, LinearLayoutManager.VERTICAL, false)
        listView.layoutManager = layoutManager
        adapter = BeerListAdapter(mutableListOf())
        adapter.context =this@SearchActivity
        adapter.onAttachedToRecyclerView(listView)
        listView.adapter = adapter
        listView.adapter = adapter
        ViewCompat.setNestedScrollingEnabled(listView, true)
        context = this.applicationContext
        adapter.clear()

        viewModel.errorThrow.observe(this, Observer {
            progressBar.visibility = View.GONE
            listView.visibility = View.VISIBLE
            isLoading = false
        })
        viewModel.beerList.observe(this, Observer {
            progressBar.visibility = View.GONE
            listView.visibility = View.VISIBLE
            adapter.search(it)
            isLoading = false
            adapter.setListInterface(object : BeerListAdapter.BeerListInterface{
                override fun onBeerSelect(beer: BeerModel) {
                    val beerIntent = Intent(this@SearchActivity,BeerViewActivity::class.java)
                    beerIntent.putExtra("BeerModel",beer)
                    startActivity(beerIntent)
                }
            })
        })

        isLoading = true
        searchText.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                val searchKeyword = searchText.query.toString()
                progressBar.visibility = View.VISIBLE
                viewModel.searchBeers(searchKeyword, this@SearchActivity, cm)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchKeyword = searchText.query.toString()
                if (searchKeyword.length > 2) {
                    progressBar.visibility = View.VISIBLE
                    viewModel.searchBeers(searchKeyword, this@SearchActivity, cm)
                }
                return false
            }

        })
//        { v, hasFocus ->
//            if(!hasFocus){
//                val searchKeyword = searchText.query.toString()
//                if(searchKeyword.length >2){
//                    progressBar.visibility = View.VISIBLE
//                    viewModel.searchBeers(searchKeyword,this.applicationContext,cm)
//                }
//            }
//        }
    }
}