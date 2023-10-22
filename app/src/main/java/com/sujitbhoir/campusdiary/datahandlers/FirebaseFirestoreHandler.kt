package com.sujitbhoir.campusdiary.datahandlers

import android.util.Log
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date

class FirebaseFirestoreHandler {

    private var db : FirebaseFirestore = Firebase.firestore
    private val TAG  = "FirebaseFirestoreHandlerTAG"
    private val userDoc = db.collection("users")

    fun updateProfilePicId(uid : String, profilePicId : String, afterUpdate : () ->Unit = {})
    {
        val profileData : HashMap<String, String> = hashMapOf(
            "profilePicId" to profilePicId
        )

        userDoc
            .document(uid)
            .set(profileData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot added with ID: ${it}")
                afterUpdate()
            }
            .addOnFailureListener {
                Log.w(TAG, "Error adding document", it)
            }
    }

    fun updateCommunityPicId(comId : String, profilePicId : String, afterUpdate : () ->Unit = {})
    {
        val profileData : HashMap<String, String> = hashMapOf(
            "profilePicId" to profilePicId
        )

        userDoc
            .document(comId)
            .set(profileData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot added with ID: ${it}")
                afterUpdate()
            }
            .addOnFailureListener {
                Log.w(TAG, "Error adding document", it)
            }
    }



    fun addUserData(
        uid : String,
        userName : String,
        name : String,
        email : String,
        campus : String,
        afterAdding : () -> Unit = {},
        onError : (exception : Exception) -> Unit = {}
    )
    {
        val userinfo : HashMap<String, String> = hashMapOf(
            "username" to userName,
            "name" to name,
            "email" to email,
            "id" to uid,
            "campus" to campus
        )

       userDoc
            .document(uid)
            .set(userinfo)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot added with ID: ${it}")
                afterAdding()

            }
            .addOnFailureListener {
                Log.w(TAG, "Error adding document", it)
                onError(it)
            }
    }

    fun getCommunitiesData(
        editor : String,
        afterLoad : (docs : List<DocumentSnapshot>) -> Unit
    )
    {

        db.collection("community")
            .whereArrayContains("editors", editor)
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "Community Doc get with id : $it")
                afterLoad(it.documents)
            }
            .addOnFailureListener{
                Log.w(TAG, "Error adding document", it)
            }
    }

    fun uploadPost(
        title : String,
        context : String,
        communityName : String,
        images : String,
        communityId : String,
        tags : List<String>,
        authUName : String,
        afterPosting : (postId : String) -> Unit
    )
    {

        val ref = Firebase.firestore.collection("posts").document()
        val data = hashMapOf<String, Any>(
            "id" to ref.id,
            "title" to title,
            "images" to images,
            "authUName" to authUName,
            "communityId" to communityId,
            "communityName" to communityName,
            "context" to context,
            "creationDate" to Date(Timestamp.now().seconds),
            "tags" to tags,
            "likes" to listOf<String>()

        )

        ref.set(data)
            .addOnSuccessListener {
                Log.d(TAG, "posted")
                afterPosting(ref.id)

            }
            .addOnFailureListener{
                Log.d(TAG, "cant post")
            }
    }




}