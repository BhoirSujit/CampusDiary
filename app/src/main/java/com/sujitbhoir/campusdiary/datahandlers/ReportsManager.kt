package com.sujitbhoir.campusdiary.datahandlers

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ReportsManager {
    val ref = Firebase.firestore.collection("reports")
    val TAG = "ReportsManagerTAG"


    fun reportUser(userid : String, yourid: String, context:String)
    {
        val doc = ref.document()

        val data = mapOf<String, Any>(
            "type" to "user",
            "id" to userid,
            "writterid" to yourid,
            "context" to context,
            "submitDate" to Timestamp.now()

        )
        doc.set(data)
            .addOnSuccessListener {
                Log.d(TAG, "report submited")
            }
    }

    fun reportCommunity(comid : String, yourid: String, context:String)
    {
        val doc = ref.document()

        val data = mapOf<String, Any>(
            "type" to "Community",
            "id" to comid,
            "writterid" to yourid,
            "context" to context,
            "submitDate" to Timestamp.now()

        )
        doc.set(data)
            .addOnSuccessListener {
                Log.d(TAG, "report submited")
            }
    }

    fun reportProduct(pid : String, yourid: String, context:String)
    {
        val doc = ref.document()

        val data = mapOf<String, Any>(
            "type" to "Product",
            "id" to pid,
            "writterid" to yourid,
            "context" to context,
            "submitDate" to Timestamp.now()

        )
        doc.set(data)
            .addOnSuccessListener {
                Log.d(TAG, "report submited")
            }
    }

    fun reportPost(pid : String, yourid: String, context:String)
    {
        val doc = ref.document()

        val data = mapOf<String, Any>(
            "type" to "Post",
            "id" to pid,
            "writterid" to yourid,
            "context" to context,
            "submitDate" to Timestamp.now()

        )
        doc.set(data)
            .addOnSuccessListener {
                Log.d(TAG, "report submited")
            }
    }

}