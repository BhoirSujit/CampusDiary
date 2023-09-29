package com.sujitbhoir.campusdiary.datahandlers

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.sql.Time

class CommunicationManager(context: Context) {

    private val db = Firebase.firestore
    private fun sessionRef() = db.collection("sessions").document()

    private val TAG = "CommunicationManagerTAG"

    fun createSession(members : List<String>, afterCreate : (id : String) -> Unit)
    {
        val ref = sessionRef()
        val id = ref.id

        val data = hashMapOf<String, Any>(
            "id" to id,
            "members" to members,
            "lastmsg" to "You can chat now",
            "lasttime" to Timestamp.now(),
            "sender" to ""
        )

        ref.set(data)
            .addOnSuccessListener {
                Log.d(TAG, "session created with : $it")
                afterCreate(id)
            }

    }

}