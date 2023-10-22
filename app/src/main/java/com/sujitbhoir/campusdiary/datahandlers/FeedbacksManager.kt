package com.sujitbhoir.campusdiary.datahandlers

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FeedbacksManager
{
    val ref = Firebase.firestore.collection("feedback")
    val TAG = "FeedbackManagerTAG"

    fun sendFeedbackPost(postid : String, rating : Int, yourid : String, context : String)
    {
        val doc = ref.document()

        val data = mapOf<String, Any>(
            "type" to "post",
            "id" to postid,
            "writterid" to yourid,
            "rating" to rating,
            "context" to context,
            "submitDate" to Timestamp.now()

        )
        doc.set(data)
            .addOnSuccessListener {
                Log.d(TAG, "feedback submited")
            }

    }

    fun sendFeedbackProduct(proid : String,  rating : Int, yourid : String, context : String)
    {
        val doc = ref.document()

        val data = mapOf<String, Any>(
            "type" to "product",
            "id" to proid,
            "writterid" to yourid,
            "rating" to rating,
            "context" to context,
            "submitDate" to Timestamp.now()

        )
        doc.set(data)
            .addOnSuccessListener {
                Log.d(TAG, "feedback submited")
            }
    }
}