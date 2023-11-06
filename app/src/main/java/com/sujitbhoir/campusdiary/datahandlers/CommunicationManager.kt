package com.sujitbhoir.campusdiary.datahandlers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.dataclasses.MessageData
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.UUID


class CommunicationManager(val context: Context) {

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private var listenerRegistration : ListenerRegistration? = null
    private var listenerRegistrationsession : ListenerRegistration? = null
    private fun sessionRef() = db.collection("sessions").document()

    private fun getChatSession(sessionId : String) = db.collection("chatrooms").document(sessionId).collection("messages")

    private val TAG = "CommunicationManagerTAG"

    private fun uniqueId() : String = (Timestamp.now().seconds + UUID.randomUUID().hashCode()).toString()
    fun getChatMediaFile(profilePicId : String) : File = File(context.filesDir.absolutePath+ File.separator + "ChatMedia", "$profilePicId.jpg")
    private fun getChatMediaRef(profilePicId : String) : StorageReference = storage.reference.child("ChatMedia/$profilePicId.jpg")

    init {
        //create path
        File(context.filesDir.absolutePath+ File.separator + "ChatMedia").let {
            if (!  it.exists())
            {
                it.mkdir()
            }
        }
    }


    fun exitSession(sessionId: String,members : List<String>, afterCreate : () -> Unit)
    {
        val ref = db.collection("sessions").document(sessionId)


        val udata = hashMapOf<String, Any>(
            "exitmebers" to members
        )

        ref.set(udata, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "session created with : $it")
                afterCreate()
            }

    }

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

    fun stopLoadingChats()
    {
        listenerRegistration?.remove()
    }

    fun stopLoadingSessions()
    {
        listenerRegistrationsession?.remove()
    }

    var sessionId : String? = null
    var eventListener : EventListener<QuerySnapshot>? = null
    var eventlistenersession : EventListener<QuerySnapshot>? = null




    fun loadChats(sessionId : String, AfterLoad : (messagesList : ArrayList<MessageData>) -> Unit)
    {


        this.sessionId = sessionId
        val eventListener : EventListener<QuerySnapshot> =
            EventListener { snapshot, e ->
                Log.d(TAG, "snapshot fired : $snapshot")
                snapshot?.let {
                    Log.d(TAG, "snapshot data are : $it")

                    val messagesData = it.documents

                    val data = ArrayList<MessageData>()
                    for (msg in messagesData) {
                        val d = msg.toObject(MessageData::class.java)!!
                        data.add(d)
                    }
                    AfterLoad(data)
                }
            }

        if (listenerRegistration == null ) {
            listenerRegistration =  getChatSession(sessionId).orderBy("time", Query.Direction.ASCENDING).addSnapshotListener(eventListener);
        }



//            getChatSession(sessionId).orderBy("time")
//
//
//
//                .addSnapshotListener { value, error ->
//                Log.d(TAG, "snapshot getted")
//
//                value?.let {
//                    Log.d(TAG, "snapshot data are : $it")
//
//                    val messagesData = it.documents
//
//                    val data = ArrayList<MessageData>()
//                    for (msg in messagesData)
//                    {
//                        val d = msg.toObject(MessageData::class.java)!!
//                        data.add(d)
//                    }
//                    AfterLoad(data)
//                }
//
//
//
//
//            }

    }

    fun startLodingChats()
    {
        if (sessionId != null && eventListener != null)
        {
            listenerRegistration = getChatSession(sessionId!!).orderBy("time").addSnapshotListener(
                eventListener!!
            );
        }

    }

    fun uploadChatMedia(uri : Uri, afterUpload : (mediaid : String) -> Unit)
    {
        // calling from global scope
        var bitmap: Bitmap? = null
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val baos = ByteArrayOutputStream()

        bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, baos)

        val data = baos.toByteArray()

        val id = uniqueId()

        val ref = getChatMediaRef(id)
        ref.putBytes(data)
            .addOnSuccessListener {
                Log.d(TAG, "successfull upload")
                afterUpload(id)


            }
            .addOnFailureListener{
                Log.d(TAG, "unsuccessfull upload")
            }


    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setChatMedia(mediaId : String, image : ImageView)
    {

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        var file = getChatMediaFile(mediaId)
        val ref = getChatMediaRef(mediaId)

        if (file.exists())
        {
            //load pitcher
            Glide.with(context)
                .load(file)
                .centerCrop()
                .placeholder(circularProgressDrawable)
                .error(R.drawable.user)
                .into(image)

        }
        else
        {
            ref.getFile(file)
                .addOnSuccessListener {
                    Log.d(TAG, "successfull saved in storage")
                    file = getChatMediaFile(mediaId)

                    //load pitcher
                    Glide.with(context)
                        .load(file)
                        .placeholder(circularProgressDrawable)
                        .centerCrop()
                        .error(R.drawable.user)
                        .into(image)

                }
                .addOnFailureListener{
                    Log.d(TAG, "unsuccessfully saved")
                }
        }
    }

    fun sendMessage(sessionId: String, msg: String, img: Uri? = null)
    {
            val ref = getChatSession(sessionId).document()
            val id = ref.id

        val time = Timestamp.now()
        val data = hashMapOf<String, Any>(
            "id" to id,
            "msg" to msg,
            "time" to time,
            "sender" to Firebase.auth.currentUser!!.uid.toString(),
        )

        if (img != null)
        {
            uploadChatMedia(img)
            {
                data.put("img", it)
                ref.set(data).addOnSuccessListener {
                    Log.d(TAG, "message send")

                    val updatedData = hashMapOf<String, Any>(
                        "lasttime" to time,
                        "sender" to Firebase.auth.currentUser!!.uid,
                    )

                    if (msg.isBlank())
                        updatedData["lastmsg"] = "image"
                    else
                        updatedData["lastmsg"] = msg

                    db.collection("sessions").document(sessionId)
                        .set(updatedData, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d(TAG, "session updated")
                        }
                }
            }
        }
        else
        {
            data.put("img", "")
            ref.set(data).addOnSuccessListener {
                Log.d(TAG, "message send")

                val updatedData = hashMapOf<String, Any>(
                    "sender" to Firebase.auth.currentUser!!.uid,
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

    fun deleteMessage(sessionId: String, msgid: String)
    {
        val ref = getChatSession(sessionId)
        ref.document(msgid)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "message deleted success fully")
                db.collection("sessions").document(sessionId)
                val updatedData = hashMapOf<String, Any>(
                    "lastmsg" to "message was deleted",
                    "sender" to Firebase.auth.currentUser!!.uid,
                )

                db.collection("sessions").document(sessionId)
                    .set(updatedData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "session updated")
                        Toast.makeText(context, "Message deleted successfully", Toast.LENGTH_SHORT).show()
                    }

            }
            .addOnFailureListener{
                Log.d(TAG, "message deleted success fully")
            }
    }



}