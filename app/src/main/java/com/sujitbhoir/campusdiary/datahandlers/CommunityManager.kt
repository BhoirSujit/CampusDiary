package com.sujitbhoir.campusdiary.datahandlers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.scale
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.dataclasses.CommunityData
import com.sujitbhoir.campusdiary.dataclasses.ProductData
import com.sujitbhoir.campusdiary.dataclasses.UserData
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.UUID

class CommunityManager(val context : Context) {

    private val db = Firebase.firestore
    private val TAG = "CommunityManagerTAG"
    private val communityRef = db.collection("community")
    private val createCommunityRef = communityRef.document()
    private val storage = Firebase.storage

    private fun getCommunityRef(id : String)  = communityRef.document(id)

    private fun uniqueId() : String = (Timestamp.now().seconds + UUID.randomUUID().hashCode()).toString()
    fun getCommunityPicFile(profilePicId : String) : File = File(context.filesDir.absolutePath+ File.separator + "CommunityPictures", "$profilePicId.jpg")
    private fun getCommunityPicRef(profilePicId : String) : StorageReference = storage.reference.child("CommunityPictures/$profilePicId.jpg")

    init {
        //create path
        File(context.filesDir.absolutePath+ File.separator + "CommunityPictures").let {
            if (!  it.exists())
            {
                it.mkdir()
            }
        }
    }


    fun getCommunitiesData(afterLoad: (arr : ArrayList<CommunityData>) -> Unit) {

        val arr = ArrayList<CommunityData>()

        communityRef
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "data are : ${it.documents}")

                for (doc in it.documents)
                {
                    val data = doc.toObject(CommunityData::class.java)!!
                    arr.add(data)
                }

                afterLoad(arr)
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }

    fun getCommunityData(id : String, afterLoad: (arr : CommunityData) -> Unit) {




        communityRef
            .document(id)
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "data are : ${it}")
                val data = it.toObject(CommunityData::class.java)!!

                afterLoad(data)
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }

    fun createCommunity(name : String, imgUri : Uri, about : String, campus : String, admin : String, tags : List<String>, afterLoad: (id : String) -> Unit)
    {
        val ref = createCommunityRef
        val id = ref.id

        val imageid = uniqueId()

        val communityInfo : HashMap<String, Any> = hashMapOf(
            "id" to id,
            "name" to name,
            "communityPicId" to imageid,
            "about" to about,
            "campus" to campus,
            "admin" to admin,
            "members" to listOf<String>(admin),
            "tags"  to tags
        )

        ref.set(communityInfo)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot added with ID: ${it}")
                //compress file
                uploadCommunityPic(imgUri, imageid)
                {
                    afterLoad(id)
                }


            }
            .addOnFailureListener {
                Log.w(TAG, "Error adding document", it)
            }
    }

    fun uploadCommunityPic(uri : Uri, id: String, afterUpload : (profilePicID : String) -> Unit)
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

        val ref = getCommunityPicRef(id)
        ref.putBytes(data)
            .addOnSuccessListener {
                Log.d(TAG, "successfull upload")
                afterUpload(id)


            }
            .addOnFailureListener{
                Log.d(TAG, "unsuccessfull upload")
            }


    }

    fun  getCommunitiesData(ids : ArrayList<String>, afterLoad : (usersData : HashMap<String, CommunityData>) -> Unit)
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
        val reqComData = HashMap<String, CommunityData>()




        //suggest //get data
        communityRef
            .where( Filter.or(
                *f
            ))
            .get()
            .addOnSuccessListener {
                //parse data
                for (doc in it.documents)
                {
                    val cData = doc.toObject(CommunityData::class.java)!!
                    Log.d(TAG, "data are : $cData")
                    reqComData.put(cData.id, cData)
                }
                afterLoad(reqComData)
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setProfilePic(profilePicID : String, image : ImageView)
    {
        if (profilePicID.isBlank())
        {
            image.setImageDrawable(context.resources.getDrawable(R.drawable.user))
        return}

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        var file = getCommunityPicFile(profilePicID)
        val ref = getCommunityPicRef(profilePicID)

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
                    file = getCommunityPicFile(profilePicID)

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

    fun joinCommunity(communityId: String, afterToggle : (members : List<String>) -> Unit)
    {
        val userid = Firebase.auth.currentUser!!.uid
        getCommunityData(communityId)
        {
            val member = ArrayList<String>()
            member.addAll(it.members)
            if (member.contains(userid)) member.remove(userid)
            else member.add(userid)

            getCommunityRef(communityId).set(
                hashMapOf(
                    "members" to member.toList()
                ), SetOptions.merge()
            ).addOnSuccessListener {
                afterToggle(member.toList())
            }
        }
    }
}