package com.example.sampleappsplash.view


import android.content.Context
import com.example.sampleappsplash.model.BeerModel
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.sampleappsplash.R
import com.example.sampleappsplash.model.NetworkInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.net.URL



class BeerListAdapter(private var beerList: MutableList<BeerModel>) : RecyclerView.Adapter<BeerListAdapter.ViewHolder>() {

    private lateinit var beerListInterface: BeerListInterface
    var context: Context? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val beerName = itemView.findViewById<TextView>(R.id.beer_name)
        val beerAbv = itemView.findViewById<TextView>(R.id.beer_abv)
        val beerPlace = itemView.findViewById<ConstraintLayout>(R.id.beer_placeholder)
        fun bind(beer: BeerModel) {
            beerName.text = beer.name
            val strabv = "ABV - ${beer.abv}"
            beerAbv.text = strabv
        }

        fun imageShow(image:Bitmap){
            CoroutineScope(Dispatchers.Main).launch {
                itemView.findViewById<ImageView>(R.id.beer_image).setImageBitmap(image)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.beer_recycler_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return beerList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val beer = beerList[position]
        var img: Bitmap? = null
        beer.image_url?.let {
            if(NetworkInfo(context?.applicationContext!!).getNetworkInfo()) {
                CoroutineScope(Dispatchers.Default).launch {
                    async {
                        img = BitmapFactory.decodeStream(
                            URL(beer.image_url).openConnection().getInputStream()
                        )
                        holder.imageShow(img!!)
                    }
                }
            }
        }
        holder.bind(beer)
        holder.beerPlace.setOnClickListener {
            beerListInterface.onBeerSelect(beer)
        }

    }

    fun clear(){
        beerList.clear()
        notifyDataSetChanged()
    }

    fun addnew(list:MutableList<BeerModel>){
        for(item in list){
            beerList.add(item)
        }
        notifyDataSetChanged()
    }

    fun search(list: MutableList<BeerModel>){
        beerList.clear()
        beerList.addAll(list)
        notifyDataSetChanged()
    }

    interface BeerListInterface{
        fun onBeerSelect(beer: BeerModel)
    }

    fun setListInterface(beerListInterFace: BeerListInterface){
        this.beerListInterface =beerListInterFace
    }
}