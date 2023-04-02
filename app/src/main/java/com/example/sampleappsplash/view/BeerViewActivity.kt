package com.example.sampleappsplash.view

import android.content.ContentValues
import com.example.sampleappsplash.model.BeerModel
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.example.sampleappsplash.R
import com.example.sampleappsplash.model.BeerDBRepository
import com.example.sampleappsplash.model.BeerDatabase
import com.example.sampleappsplash.model.NetworkInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.net.URL

class BeerViewActivity : AppCompatActivity() {

    private var beer: BeerModel? = null
    lateinit var imageBeer: ImageView
    lateinit var beerButton: ImageButton
    lateinit var beerShare: ImageButton
    lateinit var beerInfo: LinearLayout
    var beerLiked = false
    private lateinit var context: Context

    override fun onResume() {
        super.onResume()
        val intent = intent
        beer = intent?.getSerializableExtra("BeerModel") as BeerModel
        beerDataToActivity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beer_view)
        context = this.applicationContext
    }

    private fun beerDataToActivity() {
        imageBeer = findViewById(R.id.beer_image)
        val beerName = findViewById<TextView>(R.id.beer_name)
        val beerTagline = findViewById<TextView>(R.id.beer_tagline)
        val beerDescription = findViewById<TextView>(R.id.beer_description)
        val beerAbv = findViewById<TextView>(R.id.beer_abv)
        val beerPH = findViewById<TextView>(R.id.beer_ph)
        val beerIBU = findViewById<TextView>(R.id.beer_ibu)
        val beerContributedBy = findViewById<TextView>(R.id.beer_contributed_by)
        beerShare = findViewById(R.id.beer_share)
        beerShare.z = imageBeer.z + 1
        beerInfo = findViewById(R.id.beer_info_layout)

        beer?.let {
            CoroutineScope(Dispatchers.Default).launch {
                if (NetworkInfo(this@BeerViewActivity).getNetworkInfo()) {
                    beer?.image_url?.let {
                        async {
                            val img = BitmapFactory.decodeStream(
                                URL(beer?.image_url).openConnection().getInputStream()
                            )
                            setImage(img)
                        }
                    }
                }
                async {
                    val tempBeerData = BeerDBRepository(
                        BeerDatabase.getInstance(context.applicationContext).beerDao()
                    ).getBeerById(it.id)
                    beerLiked = tempBeerData.isNotEmpty()
                    changeBeerButton()
                }
            }


            CoroutineScope(Dispatchers.Main).launch {
                imageBeer.contentDescription = it.description
                beerName.text = it.name
                beerTagline.text = it.tagline
                beerDescription.text = it.description
                val abv = "${it.abv}% ABV"
                beerAbv.text = abv
                val ph = "${it.ph} PH"
                beerPH.text = ph
                val ibu = "${it.ibu} IBU"
                beerIBU.text = ibu
                val contributedBy = "Contributed By" + it.contributed_by.substringBefore("<")
                beerContributedBy.text = contributedBy
                beerButton.z = imageBeer.z + 1
            }

            beerButton = findViewById(R.id.beer_like)
            beerShare.setOnClickListener {
                val view = window.decorView.rootView
                view.isDrawingCacheEnabled = true
                beerShare.visibility = View.INVISIBLE
                beerButton.visibility = View.INVISIBLE
                val bitmap = Bitmap.createBitmap(view.drawingCache)
                view.isDrawingCacheEnabled = false
                beerShare.visibility = View.VISIBLE
                beerButton.visibility = View.VISIBLE

                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "beer_${System.currentTimeMillis()}")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                }
                val contentResolver = contentResolver
                val uri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
                )
                uri?.let {
                    contentResolver.openOutputStream(it).use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        outputStream?.flush()
                        outputStream?.close()
                    }
                }
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "image/png"
                }
                startActivity(Intent.createChooser(shareIntent, "Share Screenshot"))
            }
            beerButton.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (!beerLiked) {
                            BeerDBRepository(
                                BeerDatabase.getInstance(context.applicationContext).beerDao()
                            ).insertBeer(beer!!)
                            beerLiked = true
                            makeToast()
                        } else {
                            BeerDBRepository(
                                BeerDatabase.getInstance(context.applicationContext).beerDao()
                            ).deleteBeer(beer!!)
                            beerLiked = false
                        }
                        changeBeerButton()
                    } catch (e: Exception) {
                        Log.d("error Occurred", "${e.stackTrace}")
                    }
                }
            }
        }
    }

    private fun setImage(img: Bitmap) {
        CoroutineScope(Dispatchers.Main).launch {
            imageBeer.setImageBitmap(img)
            imageBeer.contentDescription = beer?.description
            Palette.from(img).generate {
                val dominantColor = it?.vibrantSwatch?.rgb ?: Color.BLACK
                val selectedColor = ColorUtils.setAlphaComponent(dominantColor, 128)
                beerInfo.setBackgroundColor(selectedColor)
            }
        }
    }

    private fun changeBeerButton() {
        if (beerLiked) {
            beerButton.setImageResource(R.drawable.ic_like)
        } else {
            beerButton.setImageResource(R.drawable.ic_notlike)
        }
    }

    private fun makeToast() {
        CoroutineScope(Dispatchers.Main).launch {
            if (beerLiked) {
                Toast.makeText(this@BeerViewActivity, "Added to favourite list", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    this@BeerViewActivity, "Removed to favourite list", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}