package com.sujitbhoir.campusdiary.pages.communication

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sujitbhoir.campusdiary.adapters.ChatListAdapter
import com.sujitbhoir.campusdiary.databinding.ActivityChatBinding
import com.sujitbhoir.campusdiary.datahandlers.CommunicationManager


class ChatActivity : AppCompatActivity() {

    private  lateinit var binding: ActivityChatBinding
    private lateinit var communicationManager : CommunicationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionId = intent.getStringExtra("sessionid")!!

        communicationManager = CommunicationManager(this)

        val recyclerView = binding.recycleView
        val linearLayoutManager = LinearLayoutManager(this)

        linearLayoutManager.orientation = RecyclerView.VERTICAL
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        val chatListAdapter = ChatListAdapter(this)
        chatListAdapter.setHasStableIds(true)

        communicationManager.loadChats(sessionId)
        {
            chatListAdapter.updateData(it)
            recyclerView.scrollToPosition(it.size - 1);
            val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 150)
        }
        recyclerView.adapter = chatListAdapter


        //send message
        binding.btnSend.setOnClickListener {
            if (true)
            {
                communicationManager.sendMessage(sessionId, binding.messagebox.text.toString())
                binding.messagebox.text.clear()
            }
        }





    }
}