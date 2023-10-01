package com.sujitbhoir.campusdiary.datahandlers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.shapes.Shape
import android.media.ThumbnailUtils
import android.net.wifi.WifiManager.SubsystemRestartTrackingCallback
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LAYER_TYPE_NONE
import android.widget.LinearLayout.LAYOUT_DIRECTION_RTL
import android.widget.LinearLayout.LayoutParams
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.MaterialShapeUtils
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.Shapeable
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback.ShapeProvider
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
import kotlin.coroutines.coroutineContext
import kotlin.math.abs
import kotlin.math.absoluteValue

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

    companion object {
        fun getDpfromFloat(context : Context, value : Float) : Int
        {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,  value,  context.resources.displayMetrics).toInt()
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

    fun setPostPicShapable(ID : String,  image : FrameLayout)
    {

        var file = getStorageFile(ID)
        val ref = getStorageRef(ID)

        fun load()
        {
            val uri = file.toUri()
            val option = BitmapFactory.Options()
            option.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.absolutePath, option)

            var bitmap: Bitmap? = null
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val h = option.outHeight
            val w = option.outWidth

            val ratio  = (bitmap!!.width.toDouble().div(bitmap.height) * 100).toInt()
            Log.d(TAG, "ration are : $h , $w,  $ratio")

            val newimg = ShapeableImageView(context)
            newimg.layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, when
            {
                h > w -> getDpfromFloat(context, 256F)

                else -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,  198F,  context.resources.displayMetrics).toInt()
            })





            val shapeAppearanceModel = ShapeAppearanceModel()
                        .toBuilder()
                        .setAllCorners(CornerFamily.ROUNDED, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,  12F,  context.resources.displayMetrics))
                        .build();

            newimg.shapeAppearanceModel = shapeAppearanceModel



            image.addView(newimg)


            Glide.with(context)
                .load(bitmap)
                .centerCrop()
                .placeholder(R.drawable.college_campus_rafiki)
                .into(newimg)




        }

        if (file.exists())
        {
            //load pitcher
            load()

        }
        else
        {
            ref.getFile(file)
                .addOnSuccessListener {
                    Log.d(TAG, "successfull saved in storage")
                    file = getStorageFile(ID)

                    //load pitcher
                    load()

                }
                .addOnFailureListener{
                    Log.d(TAG, "unsuccessfully saved")
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
                .load(file)
                .centerCrop()
                .placeholder(R.drawable.college_campus_rafiki)
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
                        .centerCrop()
                        .placeholder(R.drawable.college_campus_rafiki)
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