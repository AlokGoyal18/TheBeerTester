package com.example.sampleappsplash.view


import com.example.sampleappsplash.model.BeerModel
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.sampleappsplash.R
import com.example.sampleappsplash.model.BeerViewType
import com.example.sampleappsplash.viewmodel.BeerListViewModel

class BeerList() : AppCompatActivity() {

    private var isSavedList = BeerViewType.ONLINE
    private lateinit var viewModel: BeerListViewModel
    private lateinit var listView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fullProgressBar: ProgressBar
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var adapter: BeerListAdapter
    private lateinit var cm :ConnectivityManager
    private lateinit var context:Context
    var isLoading = true
    var page = 1
    val perPage = 25

    private var networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            if (::refreshLayout.isInitialized) {
                refreshLayout.post {
                    refreshListener.onRefresh()
                }
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
        }
    }

    var refreshListener = SwipeRefreshLayout.OnRefreshListener {

        if (!isLoading) {
            if(isSavedList == BeerViewType.SAVED || cm.activeNetworkInfo?.isConnected == true) {
                isLoading = true
                refreshLayout.isRefreshing = true
                page = 1
                adapter.clear()
                viewModel.loadBeer(page, perPage, isSavedList, this.applicationContext, cm)
            }
        }
        else if(cm.activeNetworkInfo?.isConnected == false){
            Toast.makeText(this@BeerList,getString(R.string.no_internet),Toast.LENGTH_SHORT).show()
            refreshLayout.isRefreshing = false
        }
    }

    val netRequest: NetworkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_VPN).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beer_list)

        cm = this@BeerList.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerNetworkCallback(
            netRequest,
            networkCallback
        )


        isSavedList = when(intent.getStringExtra("isSaved")){
            BeerViewType.SAVED.name -> BeerViewType.SAVED
            BeerViewType.ONLINE.name -> BeerViewType.ONLINE
            BeerViewType.SEARCH.name -> BeerViewType.SEARCH
            else -> BeerViewType.ONLINE
        }

        viewModel = ViewModelProvider(this).get(BeerListViewModel::class.java)
        listView = findViewById(R.id.listView)
        progressBar = findViewById(R.id.thandRakh)
        fullProgressBar = findViewById(R.id.list_progressbar)
        refreshLayout = findViewById(R.id.refreshLayout)
        val layoutManager = LinearLayoutManager(this@BeerList, LinearLayoutManager.VERTICAL, false)
        listView.layoutManager = layoutManager
        adapter = BeerListAdapter(mutableListOf())
        adapter.context =this@BeerList
        adapter.onAttachedToRecyclerView(listView)
        listView.adapter = adapter
        listView.adapter = adapter
        fullProgressBar.visibility = View.VISIBLE
        refreshLayout.setOnRefreshListener(refreshListener)
        refreshLayout.isRefreshing = false
        ViewCompat.setNestedScrollingEnabled(listView, true)
        context = this.applicationContext
        adapter.clear()

        viewModel.errorThrow.observe(this, Observer {
            fullProgressBar.visibility = View.GONE
            progressBar.visibility = View.GONE
            listView.visibility = View.VISIBLE
            isLoading = false
            refreshLayout.isRefreshing = false
            Toast.makeText(this@BeerList,getString(R.string.basic_error),Toast.LENGTH_LONG).show()
        })
        viewModel.beerList.observe(this, Observer {
            fullProgressBar.visibility = View.GONE
            progressBar.visibility = View.GONE
            listView.visibility = View.VISIBLE
            adapter.addnew(it)
            isLoading = false
            refreshLayout.isRefreshing = false
            adapter.setListInterface(object : BeerListAdapter.BeerListInterface{
                override fun onBeerSelect(beer: BeerModel) {
                    val beerIntent = Intent(this@BeerList,BeerViewActivity::class.java)
                    beerIntent.putExtra("BeerModel",beer)
                    startActivity(beerIntent)
                }

            })
        })

        if(isSavedList == BeerViewType.ONLINE) {
            listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val currentItem = layoutManager.childCount
                    val totalItem = layoutManager.itemCount
                    val scrollOutItem = layoutManager.findFirstVisibleItemPosition()
                    if (!isLoading && !refreshLayout.isRefreshing && (currentItem + scrollOutItem == totalItem) && dy > 0 && cm.activeNetworkInfo?.isConnected == true) {
                        isLoading = true
                        refreshLayout.isRefreshing = false
                        progressBar.visibility = View.VISIBLE
                        viewModel.loadBeer(page, perPage, isSavedList, context, cm)
                    }
                }
            })
        }

        isLoading = true
        progressBar.visibility = View.VISIBLE
        if(cm.activeNetworkInfo?.isConnected == true ) {
            viewModel.loadBeer(page, perPage, isSavedList, this.applicationContext, cm)
            if (isSavedList == BeerViewType.ONLINE) {
                page++
            }
        }else{
            if(isSavedList == BeerViewType.SAVED){
                viewModel.loadBeer(page, perPage, isSavedList, this.applicationContext, cm)
            }
            else {
                Toast.makeText(this@BeerList, getString(R.string.no_internet), Toast.LENGTH_SHORT)
                    .show()
                fullProgressBar.visibility = View.GONE
                progressBar.visibility = View.GONE
                refreshLayout.isRefreshing = false
            }
        }
    }
}