package com.sujitbhoir.campusdiary.datahandlers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.wifi.WifiManager.SubsystemRestartTrackingCallback
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.dataclasses.PostData
import com.sujitbhoir.campusdiary.dataclasses.ProductData
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.UUID

class PostsManager(private val context: Context) {

    private val db = Firebase.firestore
    private val TAG = "PostsManagerTAG"
    private val ref = db.collection("posts")
    private val storage = Firebase.storage
    private fun createPostRef() = db.collection("posts").document()
    fun getPostRef(id : String) = db.collection("posts").document(id)
    private fun getStorageRef(id : String) = storage.reference.child("PostsMedia/$id.jpeg")
    private fun getStorageFile(id : String) : File = File(context.filesDir.absolutePath+ File.separator + "PostsMedia", "$id.jpg")
    private fun uniqueId() : String = (Timestamp.now().seconds + UUID.randomUUID().hashCode()).toString()

    init {
        //create path
        File(context.filesDir.absolutePath+ File.separator + "PostsMedia").let {
            if (!  it.exists())
            {
                it.mkdir()
            }
        }
    }

    fun getPostData(id : String, afterLoad : (postData : PostData) -> Unit)
    {
        ref.document(id)
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "post accessed with id : $it")
                val data = it.toObject(PostData::class.java)!!
                afterLoad(data)
            }
            .addOnFailureListener {
                Log.d(TAG, "post denied  : $it")
            }
    }

    fun uploadPost(
        title : String,
        context : String,
        communityName : String,
        images : ArrayList<String>,
        communityId : String,
        tags : List<String>,
        authUName : String,
        afterPosting : (postId : String) -> Unit
    )
    {

        val ref = createPostRef()
        val id = ref.id

        val imagesIds = ArrayList<String>()

        //create images ids
        for (i in images)
            imagesIds.add(uniqueId())


        val data = hashMapOf<String, Any>(
            "id" to id,
            "title" to title,
            "images" to imagesIds.toList(),
            "authUName" to authUName,
            "communityId" to communityId,
            "communityName" to communityName,
            "context" to context,
            "creationDate" to Timestamp.now().seconds.toString(),
            "tags" to tags,
            "likes" to listOf<String>()

        )

        ref.set(data)
            .addOnSuccessListener {
                Log.d(TAG, "posted")

                Log.d(TAG, "snapshot added with $it")
                var c = 0;
                uploadImages(images, imagesIds)
                {
                    c += 1
                    if (images.size == c) afterPosting(id)
                }

            }
            .addOnFailureListener{
                Log.d(TAG, "cant post")
            }
    }

    fun uploadImages(files : ArrayList<String>, imagesIds : ArrayList<String>, afterLoad : () -> Unit)
    {
        var i = 0;
        for (file in files) {
            // calling from global scope
            var bitmap: Bitmap? = null
            try {
                bitmap = BitmapFactory.decodeFile(file)
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

    fun setPostPic(ID : String,  image : ImageView)
    {

        var file = getStorageFile(ID)
        val ref = getStorageRef(ID)

        if (file.exists())
        {
            //load pitcher
            Glide.with(context)
                .load(ref)
                .centerCrop()
                .into(image)
                .onLoadFailed(context.resources.getDrawable(R.drawable.user))

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
                        .centerCrop()
                        .into(image)
                        .onLoadFailed(context.resources.getDrawable(R.drawable.user))

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
                carousel.registerLifecycle(lifecycle = lifecycle)
                carousel.setData(list)

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
                        carousel.registerLifecycle(lifecycle = lifecycle)
                        carousel.setData(list)

                    }
                    .addOnFailureListener{
                        Log.d(TAG, "unsuccessfully saved")
                    }
            }
        }


    }

    fun likeAPost(postId: String, afterToggle : (likes : List<String>) -> Unit)
    {
        val userid = Firebase.auth.currentUser!!.uid
        getPostData(postId)
        {
            val likes = ArrayList<String>()
            likes.addAll(it.likes)
            if (likes.contains(userid)) likes.remove(userid)
            else likes.add(userid)

            getPostRef(postId).set(
                hashMapOf(
                    "likes" to likes.toList()
                ), SetOptions.merge()
            ).addOnSuccessListener {
                afterToggle(likes.toList())
            }
        }
    }



}