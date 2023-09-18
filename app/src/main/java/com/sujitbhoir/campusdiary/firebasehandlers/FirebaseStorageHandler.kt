package com.sujitbhoir.campusdiary.firebasehandlers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.helperclass.DataHandler
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

    fun uploadProfilePic(uri : Uri, afterUpload : (profilePicID : String) -> Unit)
    {
        //compress file
        var bitmap: Bitmap? = null
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 2, baos)

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
    private fun uniqueId() : String = (Timestamp.now().seconds + UUID.randomUUID().hashCode()).toString()
    private fun getProfilePicFile(profilePicId : String) : File = File(context.filesDir, "$profilePicId.jpg")
    private fun getProfilePicRef(profilePicId : String) : StorageReference = storage.reference.child("ProfilePictures/$profilePicId.jpg")

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
}