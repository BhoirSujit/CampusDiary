package com.sujitbhoir.campusdiary.datahandlers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.format.DateFormat
import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide

import com.bumptech.glide.request.target.CustomTarget
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.dataclasses.ProductData

import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class MarketplaceManager(val context: Context)
{
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val TAG = "MarketplaceManagerTAG"

    private fun createProductRef() = db.collection("products").document()
    fun getProductRef(id : String) = db.collection("products").document(id)
    private fun getStorageRef(id : String) = storage.reference.child("ProductsMedia/$id.jpeg")
    private fun getStorageFile(id : String) : File = File(context.filesDir.absolutePath+ File.separator + "ProductsMedia", "$id.jpg")
    private fun uniqueId() : String = (Timestamp.now().seconds + UUID.randomUUID().hashCode()).toString()

    init {
        //create path
        File(context.filesDir.absolutePath+ File.separator + "ProductsMedia").let {
            if (!  it.exists())
            {
                it.mkdir()
            }
        }
    }




    fun registerProduct(
        name : String,
        details : String,
        price : String,
        condition : String,
        images : ArrayList<String>,
        contactWhatsapp : String,
        tags : List<String>,
        campus : String,
        sellerId : String,
        sellerName : String,
        sellerPic : String,
        afterRegister : (id : String) -> Unit
    )
    {
        val ref = createProductRef()
        val id = ref.id

        val imagesIds = ArrayList<String>()

        //create images ids
        for (i in images)
            imagesIds.add(uniqueId())


        val productInfo : HashMap<String, Any> = hashMapOf(
            "id" to id,
            "name" to name,
            "details" to details,
            "price" to price,
            "condition" to condition,
            "images" to imagesIds.toList(),
            "contactWhatsapp" to contactWhatsapp,
            "tags" to tags,
            "campus" to campus,
            "sellerId" to sellerId,
            "sellerName" to sellerName,
            "sellerPic" to sellerPic,
            "uploadDate" to Timestamp.now().seconds.toString()
        )

        ref.set(productInfo)
            .addOnSuccessListener {
                Log.d(TAG, "snapshot added with $it")
                var c = 0;
                uploadImages(images, imagesIds)
                {
                    c += 1
                    if (images.size == c) afterRegister(id)
                }
            }

    }

    private fun decodeUri(context: Context, uri: Uri): Bitmap? {
        try {
            // Open an input stream from the Uri
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

            // Decode the stream into a Bitmap
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Close the input stream
            inputStream?.close()

            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    fun uploadImages(files : ArrayList<String>, imagesIds : ArrayList<String>, afterLoad : () -> Unit)
    {
        var i = 0;
        for (file in files) {
            // calling from global scope
            var bitmap: Bitmap? = null
            try {
                bitmap = decodeUri(context, file.toUri())
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val baos = ByteArrayOutputStream()

            bitmap?.compress(Bitmap.CompressFormat.JPEG, 20, baos)

            val data = baos.toByteArray()

            val mediaId = imagesIds[i++]

            val ref = getStorageRef(mediaId)
            ref.putBytes(data)
                .addOnSuccessListener {
                    Log.d(TAG, "successfull upload")
                    afterLoad()


                }
                .addOnFailureListener {
                    Log.d(TAG, "unsuccessfull upload")
                }
        }
    }
    fun getMedia(id : String, afterUpload : (file : File) -> Unit)
    {
        val ref = getStorageRef(id)
        var file = getStorageFile(id)


        if (file.exists())
        {
            //load pitcher
            afterUpload(file)

        }
        else
        {
            ref.getFile(file)
                .addOnSuccessListener {
                    Log.d(TAG, "successfull saved in storage")
                    file = getStorageFile(id)
                    //load pitcher
                    afterUpload(file)


                }
                .addOnFailureListener{
                    Log.d(TAG, "unsuccessfully saved")
                }
        }
    }
    fun getProductDetails(id : String) : ProductData
    {


        return ProductData()
    }
    fun withdrawProduct(id : String, afterLoad : (it : Any) -> Unit)
    {

    }

    fun getProductsData(filter : Filter,afterLoad: (arr : ArrayList<ProductData>) -> Unit) {

        val arr = ArrayList<ProductData>()

        db.collection("products")
            .where(filter)
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "data are : ${it.documents}")

                for (doc in it.documents)
                {
                    val proData = doc.toObject(ProductData::class.java)!!
                    arr.add(proData)
                }

                afterLoad(arr)
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }



    fun getProductsData(afterLoad: (arr : ArrayList<ProductData>) -> Unit) {

        val arr = ArrayList<ProductData>()

        db.collection("products")
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "data are : ${it.documents}")

                for (doc in it.documents)
                {
                    val proData = doc.toObject(ProductData::class.java)!!
                    arr.add(proData)
                }

                afterLoad(arr)
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }


    fun getProductsDataByCategory(cat : String,afterLoad: (arr : ArrayList<ProductData>) -> Unit) {

        val arr = ArrayList<ProductData>()

        db.collection("products")
            .whereArrayContains("tags" , cat)
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "data are : ${it.documents}")

                for (doc in it.documents)
                {
                    val proData = doc.toObject(ProductData::class.java)!!
                    arr.add(proData)
                }

                afterLoad(arr)
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }

    fun getProductData(id : String, afterLoad: (arr : ProductData) -> Unit) {

        val data = ProductData()

        db.collection("products")
            .document(id)
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "data are : ${it}")
                val data = it.toObject(ProductData::class.java)!!

                afterLoad(data)
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }

    fun getProductDataBySearch(search : String, afterLoad: (arr : ArrayList<ProductData>) -> Unit) {

        val arr = ArrayList<ProductData>()

        db.collection("products")
            .orderBy("name")
            .startAt(search).endAt(search + "\uf8ff")
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "data are : ${it.documents}")

                for (doc in it.documents)
                {
                    val proData = doc.toObject(ProductData::class.java)!!
                    arr.add(proData)
                }

                afterLoad(arr)
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }


    fun setProductPic(ID : String,  image : ImageView)
    {

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        var file = getStorageFile(ID)
        val ref = getStorageRef(ID)

        if (file.exists())
        {
            //load pitcher
            Glide.with(context)
                .load(file)
                .placeholder(circularProgressDrawable)
                .centerCrop()
                .error(R.drawable.production_quantity_limits_24px)
                .into(image)


        }
        else
        {
            ref.getFile(file)
                .addOnSuccessListener {
                    Log.d(TAG, "successfull saved in storage")
                    file = getStorageFile(ID)

                    //load pitcher
                    Glide.with(context)
                        .load(file)
                        .placeholder(circularProgressDrawable)
                        .centerCrop()
                        .error(R.drawable.production_quantity_limits_24px)
                        .into(image)
                        
                }
                .addOnFailureListener{
                    Log.d(TAG, "unsuccessfully saved")
                }
        }
    }

    fun setCarouselImages(images : List<String>, carousel : ImageCarousel, lifecycle: Lifecycle)
    {
        val list = mutableListOf<CarouselItem>()

        for (img in images)
        {
            var file = getStorageFile(img)
            val ref = getStorageRef(img)

            if (file.exists())
            {
                //load pitcher
                list.add(
                    CarouselItem(
                        imageUrl = file.absolutePath
                    )
                )

            }
            else
            {
                ref.getFile(file)
                    .addOnSuccessListener {
                        Log.d(TAG, "successfull saved in storage")
                        file = getStorageFile(img)

                        //load pitcher
                        list.add(
                            CarouselItem(
                                imageUrl = file.absolutePath
                            )
                        )

                    }
                    .addOnFailureListener{
                        Log.d(TAG, "unsuccessfully saved")
                    }
            }
        }

        carousel.registerLifecycle(lifecycle = lifecycle)
        carousel.setData(list)
    }

    fun removeProduct(id: String, function: () -> Unit) {
        getProductRef(id).delete()
            .addOnSuccessListener {
                function()
            }
            .addOnFailureListener {
                Log.d(TAG, "Cant removed item")
            }
    }

}