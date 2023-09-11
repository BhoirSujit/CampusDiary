package com.sujitbhoir.campusdiary.helperclass

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.dataclasses.UserData


import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.Charset


class DataHandler {
    private val TAG = "datahandlerTAG"

    fun getUserData(baseContext : Context): UserData? {



        val file = File(baseContext.filesDir,"user_data.json")
        var json: String = "{}"

        if (file.exists())
        {
            try {
                val inputStream: InputStream = FileInputStream(file)
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                json = String(buffer, Charset.defaultCharset())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val gson = Gson()
        return gson.fromJson(json, UserData::class.java)
    }

    fun updateUserData(baseContext: Context)
    {
        //get user info
        val db = Firebase.firestore
        val auth = Firebase.auth

        db.collection("users").document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {

                Log.d(TAG, "data are : ${it.data}")
                if (it != null && it.exists()) {
                    //setting data
                    val userData = it.toObject(UserData::class.java)

                    val gson = Gson()
                    val json = gson.toJson(userData)
                    val fileContents = json.toByteArray()
                    val file = File(baseContext.filesDir,"user_data.json")
                    try {
                        FileOutputStream(file).use {
                            it.write(fileContents)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    Log.d(TAG, "main TAG , fname : ${userData}")
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }

    fun setProfilePic(context: Context, uid: String, image : ImageView)
    {
        FirebaseApp.initializeApp(context)

        //image request
        val requestOptions = RequestOptions()
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            //.override(100, 100)

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()


        var file = File(context.filesDir, "$uid.png")
        Log.d(TAG, "image name $uid.png")
        val ref = Firebase.storage.reference.child("userspic/$uid.png")
        if (true)//if (file.exists())
        {
            //load pitcher


            Glide.with(context)
                .load(ref)
                .apply(requestOptions)
                .placeholder(circularProgressDrawable)
                .into(image)
                .onLoadFailed(context.resources.getDrawable(R.drawable.user))

        }
        else
        {
            ref.getFile(file)
                .addOnSuccessListener {
                    Log.d(TAG, "successfull saved")
                    file = File(context.filesDir, "$uid.png")

                    //load pitcher
                    Glide.with(context)
                        .load(file)
                        .placeholder(circularProgressDrawable)
                        .apply(requestOptions)
                        .into(image)
                        .onLoadFailed(context.resources.getDrawable(R.drawable.user))

                }
                .addOnFailureListener{
                    Log.d(TAG, "unsuccessfull saved")
                }
        }
    }

    fun setCommunityPic(context: Context, id: String, image : ImageView)
    {
        FirebaseApp.initializeApp(context)
        //image request
        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .circleCrop()
        //.override(100, 100)

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()


        var file = File(context.filesDir, "$id.png")
        Log.d(TAG, "image name $id.png")
        val ref = Firebase.storage.reference.child("communityIcon/${id}.png")
        if (true)
        {
            //load pitcher
            Glide.with(context)
                .load(ref)
                .apply(requestOptions)
                .placeholder(circularProgressDrawable)
                .into(image)
                .onLoadFailed(context.resources.getDrawable(R.drawable.user))

        }
        else
        {
            ref.getFile(file)
                .addOnSuccessListener {
                    Log.d(TAG, "successfull saved")
                    file = File(context.filesDir, "$id.png")

                    //load pitcher
                    Glide.with(context)
                        .load(file)
                        .placeholder(circularProgressDrawable)
                        .apply(requestOptions)
                        .into(image)
                        .onLoadFailed(context.resources.getDrawable(R.drawable.user))

                }
                .addOnFailureListener{
                    Log.d(TAG, "unsuccessfull saved")
                }
        }
    }

    fun getProfilePic(context : Context, uid :  String) : File
    {
        var file = File(context.filesDir, "$uid.png")
        val ref = Firebase.storage.reference.child("userspic/$uid.png")
        if (file.exists())
        {
//            ref.metadata.addOnSuccessListener {
//                Log.d(TAG, "${it.updatedTimeMillis} > ${file.lastModified()}")
//                if (it.updatedTimeMillis > file.lastModified())
//                {
//                    ref.getFile(file)
//                        .addOnSuccessListener {
//                            Log.d(TAG, "successfull saved")
//                            file = File(context.filesDir, "$uid.jpg")
//                        }
//                        .addOnFailureListener{
//                            Log.d(TAG, "unsuccessfull saved")
//                        }
//                }
//            }
        }
        else
        {
            ref.getFile(file)
                .addOnSuccessListener {
                    Log.d(TAG, "successfull saved")
                    file = File(context.filesDir, "$uid.png")
                }
                .addOnFailureListener{
                    Log.d(TAG, "unsuccessfull saved")
                }
        }

        return file
    }

    fun setProfilePic(context: Context, uid: String, image: CustomTarget<Drawable>) {
        FirebaseApp.initializeApp(context)
        //image request
        val requestOptions = RequestOptions()
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
        //.override(100, 100)

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        var file = File(context.filesDir, "$uid.png")
        Log.d(TAG, "image name $uid.png")
        val ref = Firebase.storage.reference.child("userspic/$uid.png")
        if (true)
        {
            //load pitcher
            Glide.with(context)
                .load(ref)
                .placeholder(circularProgressDrawable)
                .apply(requestOptions)
                .into(image)
                .onLoadFailed(context.resources.getDrawable(R.drawable.user))
        }
        else
        {
            ref.getFile(file)
                .addOnSuccessListener {
                    Log.d(TAG, "successfull saved")
                    file = File(context.filesDir, "$uid.png")

                    //load pitcher
                    Glide.with(context)
                        .load(file)
                        .placeholder(circularProgressDrawable)
                        .apply(requestOptions)
                        .into(image)
                        .onLoadFailed(context.resources.getDrawable(R.drawable.user))

                }
                .addOnFailureListener{
                    Log.d(TAG, "unsuccessfull saved")
                }
        }
    }
}

