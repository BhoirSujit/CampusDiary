package com.sujitbhoir.campusdiary.datahandlers

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.bottomsheet.UserBottomSheet
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.pages.ContactListAdapter

class UsersManager(context: Context) {

    private val db = Firebase.firestore
    private val TAG = "UsersManagerTAG"

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
}