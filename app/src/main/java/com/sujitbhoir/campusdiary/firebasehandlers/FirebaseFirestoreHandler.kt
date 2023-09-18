package com.sujitbhoir.campusdiary.firebasehandlers

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.MainActivity
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import com.sujitbhoir.campusdiary.pages.Profile
import com.sujitbhoir.campusdiary.settings.ManageInterests

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




}