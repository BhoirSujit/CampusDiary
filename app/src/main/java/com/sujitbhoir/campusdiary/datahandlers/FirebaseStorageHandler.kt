package com.sujitbhoir.campusdiary.datahandlers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.scale
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.sujitbhoir.campusdiary.R
import kotlinx.coroutines.DelicateCoroutinesApi
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.UUID

class FirebaseStorageHandler(val context : Context) {

    private var storage : FirebaseStorage = Firebase.storage
    private val TAG = "FirebaseStorageHandlerTAG"

    init
    {
        FirebaseApp.initializeApp(context)
    }

    fun getPostMedia(postid : String, afterUpload : (file : File) -> Unit)
    {
        val ref = getPostMediaRef(postid)
        var file = getProfilePicFile(postid)


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
                    file = getProfilePicFile(postid)

                    //load pitcher
                    afterUpload(file)


                }
                .addOnFailureListener{
                    Log.d(TAG, "unsuccessfully saved")
                }
        }
    }

    fun uploadPostMedia(files : ArrayList<String>, postid : String, afterUpload : () -> Unit)
    {

        var i = 1;
        for (file in files)
        {
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

            val mediaId = "$postid${i++}"

            val ref = getPostMediaRef(mediaId)
            ref.putBytes(data)
                .addOnSuccessListener {
                    Log.d(TAG, "successfull upload")
                    afterUpload()


                }
                .addOnFailureListener{
                    Log.d(TAG, "unsuccessfull upload")
                }
        }


    }


    @OptIn(DelicateCoroutinesApi::class)
    fun uploadProfilePic(uri : Uri, afterUpload : (profilePicID : String) -> Unit)
    {
        // calling from global scope
        var bitmap: Bitmap? = null
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val baos = ByteArrayOutputStream()

        fun getSquareCropDimensionForBitmap(bitmap : Bitmap) : Int
        {
            //use the smallest dimension of the image to crop to
            return Math.min(bitmap.width, bitmap.height);
        }

        val dimension = getSquareCropDimensionForBitmap(bitmap!!);




        bitmap =  ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension).scale(512,512, true)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)

        val data = baos.toByteArray()

        val profilePicID = uniqueId()

        val ref = getProfilePicRef(profilePicID)
        ref.putBytes(data)
            .addOnSuccessListener {
                Log.d(TAG, "successfull upload")
                afterUpload(profilePicID)


            }
            .addOnFailureListener{
                Log.d(TAG, "unsuccessfull upload")
            }


    }

    fun uploadCommunityPic(uri : Uri, afterUpload : (profilePicID : String) -> Unit)
    {
        // calling from global scope
        var bitmap: Bitmap? = null
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val baos = ByteArrayOutputStream()

        fun getSquareCropDimensionForBitmap(bitmap : Bitmap) : Int
        {
            //use the smallest dimension of the image to crop to
            return Math.min(bitmap.width, bitmap.height);
        }

        val dimension = getSquareCropDimensionForBitmap(bitmap!!);




        bitmap =  ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension).scale(512,512, true)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)

        val data = baos.toByteArray()

        val profilePicID = uniqueId()

        val ref = getCommunityPicRef(profilePicID)
        ref.putBytes(data)
            .addOnSuccessListener {
                Log.d(TAG, "successfull upload")
                afterUpload(profilePicID)


            }
            .addOnFailureListener{
                Log.d(TAG, "unsuccessfull upload")
            }


    }
    private fun uniqueId() : String = (Timestamp.now().seconds + UUID.randomUUID().hashCode()).toString()
    private fun getProfilePicFile(profilePicId : String) : File = File(context.filesDir, "$profilePicId.jpg")
    private fun getProfilePicRef(profilePicId : String) : StorageReference = storage.reference.child("ProfilePictures/$profilePicId.jpg")

    private fun getCommunityPicFile(profilePicId : String) : File = File(context.filesDir, "$profilePicId.jpg")
    private fun getCommunityPicRef(profilePicId : String) : StorageReference = storage.reference.child("CommunityPictures/$profilePicId.jpg")

    private fun getPostMediaRef(mediaID : String) : StorageReference = storage.reference.child("PostMedia/$mediaID.jpg")

    fun setProfilePic(profilePicID : String,  image : ImageView)
    {

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        var file = getProfilePicFile(profilePicID)
        val ref = getProfilePicRef(profilePicID)

        if (file.exists())
        {
            //load pitcher
            Glide.with(context)
                .load(ref)
                .circleCrop()
                .placeholder(circularProgressDrawable)
                .into(image)
                .onLoadFailed(context.resources.getDrawable(R.drawable.user))

        }
        else
        {
            ref.getFile(file)
                .addOnSuccessListener {
                    Log.d(TAG, "successfull saved in storage")
                    file = getProfilePicFile(profilePicID)

                    //load pitcher
                    Glide.with(context)
                        .load(file)
                        .placeholder(circularProgressDrawable)
                        .circleCrop()
                        .into(image)
                        .onLoadFailed(context.resources.getDrawable(R.drawable.user))

                }
                .addOnFailureListener{
                    Log.d(TAG, "unsuccessfully saved")
                }
        }
    }

    fun setProfilePic(profilePicID : String,  image: CustomTarget<Drawable>)
    {

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        var file = getProfilePicFile(profilePicID)
        val ref = getProfilePicRef(profilePicID)

        if (file.exists())
        {
            //load pitcher
            Glide.with(context)
                .load(ref)
                .circleCrop()
                .placeholder(circularProgressDrawable)
                .into(image)
                .onLoadFailed(context.resources.getDrawable(R.drawable.user))

        }
        else
        {
            ref.getFile(file)
                .addOnSuccessListener {
                    Log.d(TAG, "successfull saved in storage")
                    file = getProfilePicFile(profilePicID)

                    //load pitcher
                    Glide.with(context)
                        .load(file)
                        .placeholder(circularProgressDrawable)
                        .circleCrop()
                        .into(image)
                        .onLoadFailed(context.resources.getDrawable(R.drawable.user))

                }
                .addOnFailureListener{
                    Log.d(TAG, "unsuccessfully saved")
                }
        }
    }

    fun setCommunityPic(comProfilePic: String, image: ImageView) {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        var file = getCommunityPicFile(comProfilePic)
        val ref = getCommunityPicRef(comProfilePic)

        if (file.exists())
        {
            //load pitcher
            Glide.with(context)
                .load(ref)
                .circleCrop()
                .placeholder(circularProgressDrawable)
                .into(image)
                .onLoadFailed(context.resources.getDrawable(R.drawable.user))

        }
        else
        {
            ref.getFile(file)
                .addOnSuccessListener {
                    Log.d(TAG, "successfull saved in storage")
                    file = getProfilePicFile(comProfilePic)

                    //load pitcher
                    Glide.with(context)
                        .load(file)
                        .placeholder(circularProgressDrawable)
                        .circleCrop()
                        .into(image)
                        .onLoadFailed(context.resources.getDrawable(R.drawable.user))

                }
                .addOnFailureListener{
                    Log.d(TAG, "unsuccessfully saved")
                }
        }
    }
}