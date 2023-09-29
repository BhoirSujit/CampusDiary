package com.sujitbhoir.campusdiary.datahandlers


import android.content.Context
import android.graphics.Bitmap
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.dataclasses.UserData
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.UUID


class UsersManager(private val context: Context) {


    private val db = Firebase.firestore
    private val TAG = "UsersManagerTAG"
    private val storage = Firebase.storage
    private val userRef = db.collection("users")

    private fun uniqueId() : String = (Timestamp.now().seconds + UUID.randomUUID().hashCode()).toString()
    private fun getProfilePicFile(profilePicId : String) : File = File(context.filesDir.absolutePath+ File.separator + "ProfilePictures", "$profilePicId.jpg")
    private fun getProfilePicRef(profilePicId : String) : StorageReference = storage.reference.child("ProfilePictures/$profilePicId.jpg")

    private fun getStorageRef(id : String) = storage.reference.child("ProfilePictures/$id.jpg")
    private fun getStorageFile(id : String) : File = File(context.filesDir.absolutePath+ File.separator + "ProfilePictures", "$id.jpg")

    init {
        //create path
        File(context.filesDir.absolutePath+ File.separator + "ProfilePictures").let {
            if (!  it.exists())
            {
                it.mkdir()
            }
        }
    }

    fun  getUserData(id : String, afterLoad : (userData : UserData) -> Unit)
    {
        //suggest //get data
        db.collection("users")
            .whereEqualTo("id" , id)
            .get()
            .addOnSuccessListener {
                //parse data
                for (doc in it.documents)
                {
                    val userData = doc.toObject(UserData::class.java)!!
                    Log.d(TAG, "data are : $userData")
                    afterLoad(userData)
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }

    fun  getUsersData(ids : ArrayList<String>, afterLoad : (usersData : HashMap<String, UserData>) -> Unit)
    {

        //create filters
        val filters = ArrayList<Filter>()
        for (id in ids)
        {
            filters.add(Filter.equalTo("id",id))
        }

        var f = arrayOfNulls<Filter>(filters.size)
        f = filters.toArray(f)

        //
        val reqUserData = HashMap<String, UserData>()




        //suggest //get data
        db.collection("users")
            .where( Filter.or(
                *f
            ))
            .get()
            .addOnSuccessListener {
                //parse data
                for (doc in it.documents)
                {
                    val userData = doc.toObject(UserData::class.java)!!
                    Log.d(TAG, "data are : $userData")
                    reqUserData.put(userData.id, userData)
                }
                afterLoad(reqUserData)
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }

    fun getMyData(): UserData? {
        val file = File(context.filesDir,"user_data.json")
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

    fun updateUserData(baseContext: Context, afterUpload: () -> Unit= {})
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
                    afterUpload()
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }

    fun removeProfilePic(uid : String, afterUpload : () -> Unit)
    {
        val profileData : HashMap<String, String> = hashMapOf(
            "profilePicId" to ""
        )

        userRef
            .document(uid)
            .set(profileData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot added with ID: ${it}")
                afterUpload()
            }
            .addOnFailureListener {
                Log.w(TAG, "Error adding document", it)
            }
    }

    fun uploadProfilePic(uid : String, uri : Uri, afterUpload : (profilePicID : String) -> Unit)
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
                val profileData : HashMap<String, String> = hashMapOf(
                    "profilePicId" to profilePicID
                )

                userRef
                    .document(uid)
                    .set(profileData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot added with ID: ${it}")
                        afterUpload(profilePicID)
                    }
                    .addOnFailureListener {
                        Log.w(TAG, "Error adding document", it)
                    }



            }
            .addOnFailureListener{
                Log.d(TAG, "unsuccessfull upload")
            }


    }

    fun setProfilePic(profilePicID : String,  image : ImageView)
    {
        Log.d(TAG, "images loading with id $profilePicID")
        if (profilePicID.isBlank()) {
            image.setImageDrawable(context.getDrawable(R.drawable.user))
            return

        }

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
                .load(file)
                .circleCrop()
                .error(R.drawable.user)
                .placeholder(circularProgressDrawable)
                .into(image)

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
                        .error(R.drawable.user)
                        .into(image)


                }
                .addOnFailureListener{
                    Log.d(TAG, "unsuccessfully saved")
                }
        }
    }

    fun setProfilePic(profilePicID : String,  image: CustomTarget<Drawable>)
    {
        Log.d(TAG, "images loading with id $profilePicID")
        if (profilePicID.isBlank()) {
            //image.setImageDrawable(context.resources.getDrawable(R.drawable.user))
            return

        }

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
                .load(file)
                .circleCrop()
                .placeholder(circularProgressDrawable)
                .error(R.drawable.user)
                .into(image)



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
                        .error(R.drawable.user)
                        .into(image)

                }
                .addOnFailureListener{
                    Log.d(TAG, "unsuccessfully saved")
                }
        }
    }
}