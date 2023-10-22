package com.sujitbhoir.campusdiary.pages.communication

import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaType
import com.nareshchocha.filepickerlibrary.ui.FilePicker
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.adapters.ChatListAdapter
import com.sujitbhoir.campusdiary.databinding.ActivityChatBinding
import com.sujitbhoir.campusdiary.dataclasses.SessionData
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.CommunicationManager
import com.sujitbhoir.campusdiary.datahandlers.UsersManager


class ChatActivity : AppCompatActivity() {

    private  lateinit var binding: ActivityChatBinding
    private lateinit var communicationManager : CommunicationManager
    private val TAG = "ChatActivityTAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var uri : Uri? = null

        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }

        val sessionId = intent.getStringExtra("sessionid")!!

        communicationManager = CommunicationManager(this)



        Firebase.firestore.collection("sessions").document(sessionId)
            .get()
            .addOnSuccessListener {
                val sessionData = it.toObject(SessionData::class.java)!!
                val tmpArr = ArrayList<String>()
                tmpArr.addAll(sessionData.members)
                UsersManager(this).getUsersData(tmpArr)
                {
                    loadChat(it)
                    setSessionDetails(it)
                }
                Log.d(TAG, "data are : ${it}")

                //exit session
                binding.exitsession.setOnClickListener {
                    val em = ArrayList<String>(sessionData.exitmebers)
                    em.add(Firebase.auth.currentUser!!.uid)
                    val d = MaterialAlertDialogBuilder(this)
                        .setTitle("Do you want to exit from session")
                        .setMessage("Once you exit you cannot join again")
                        .setPositiveButton("Exit"){ a, b ->
                            communicationManager.exitSession(sessionId, em.toList())
                            {
                                Toast.makeText(this, "Exited From session", Toast.LENGTH_LONG).show()
                                finish()
                            }
                        }
                        .setNegativeButton("Not Now"){a, b ->
                            a.dismiss()
                        }
                        .show()
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }




        //send message
        binding.btnSend.setOnClickListener {
            if (true)
            {
                if (uri != null)
                    communicationManager.sendMessage(sessionId, binding.messagebox.text.toString(), uri)
                else if (binding.messagebox.text.isNotBlank())
                    communicationManager.sendMessage(sessionId, binding.messagebox.text.toString())
                else
                    Toast.makeText(this, "Please enter something first", Toast.LENGTH_LONG).show()
                binding.messagebox.text.clear()
                uri = null
                binding.imageContainer.visibility = View.GONE

            }
        }

        //image sending style
        binding.imageContainer.visibility = View.GONE



        //upload pic
        val resultActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            if (it.resultCode == RESULT_OK && it.data?.data != null)
            {
                uri = it.data?.data!!
                binding.imageContainer.visibility = View.VISIBLE

                Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .into(binding.sendingImage)
            }
        }

        binding.btnmedia.setOnClickListener {
            resultActivity.launch(
                FilePicker.Builder(this)
                    .pickMediaBuild(
                        PickMediaConfig(
                            mPickMediaType = PickMediaType.ImageOnly,
                            allowMultiple = false,

                            )
                    )
            )
        }

        //discard image
        binding.discardimage.setOnClickListener {
            uri = null
            binding.imageContainer.visibility = View.GONE
        }







    }
    fun setSessionDetails(usersData : HashMap<String, UserData>)
    {
        for (u in usersData)
        {
            if (u.value.id != Firebase.auth.currentUser!!.uid)
            {
                UsersManager(this).setProfilePic(u.value.profilePicId, binding.sesProPic)
                binding.sessionname.text = u.value.name
            }

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        communicationManager.stopLoadingChats()
    }



    fun loadChat(usersData : HashMap<String, UserData>)
    {
        val sessionId = intent.getStringExtra("sessionid")!!

        communicationManager = CommunicationManager(this)

        val recyclerView = binding.recycleView
        val linearLayoutManager = LinearLayoutManager(this)

        linearLayoutManager.orientation = RecyclerView.VERTICAL
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        val chatListAdapter = ChatListAdapter(this, sessionId, usersData)
        chatListAdapter.setHasStableIds(true)

        communicationManager.loadChats(sessionId)
        {
            chatListAdapter.updateData(it)
            recyclerView.scrollToPosition(it.size - 1);
            val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 150)
        }
        recyclerView.adapter = chatListAdapter


    }
}