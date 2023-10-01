package com.sujitbhoir.campusdiary.datahandlers

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.dataclasses.MessageData
import com.sujitbhoir.campusdiary.dataclasses.ProductData
import java.sql.Time


class CommunicationManager(context: Context) {

    private val db = Firebase.firestore
    private fun sessionRef() = db.collection("sessions").document()

    private fun getChatSession(sessionId : String) = db.collection("chatrooms").document(sessionId).collection("messages")

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

    fun loadChats(sessionId : String, AfterLoad : (messagesList : ArrayList<MessageData>) -> Unit)
    {
            getChatSession(sessionId).orderBy("time")
                .addSnapshotListener { value, error ->
                Log.d(TAG, "snapshot getted")

                value?.let {
                    Log.d(TAG, "snapshot data are : $it")

                    val messagesData = it.documents

                    val data = ArrayList<MessageData>()
                    for (msg in messagesData)
                    {
                        val d = msg.toObject(MessageData::class.java)!!
                        data.add(d)
                    }
                    AfterLoad(data)
                }




            }
    }

    fun sendMessage(sessionId: String, msg : String, img : Uri = Uri.EMPTY)
    {
            val ref = getChatSession(sessionId).document()
            val id = ref.id

        val time = Timestamp.now()

            val data = hashMapOf<String, Any>(
                "id" to id,
                "msg" to msg,
                "time" to time,
                "sender" to Firebase.auth.currentUser!!.uid
            )

            ref.set(data).addOnSuccessListener {
                Log.d(TAG, "message send")

                val updatedData = hashMapOf<String, Any>(
                    "lastmsg" to msg,
                    "lasttime" to time
                )

                db.collection("sessions").document(sessionId)
                    .set(updatedData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "session updated")
                    }
            }
    }

}