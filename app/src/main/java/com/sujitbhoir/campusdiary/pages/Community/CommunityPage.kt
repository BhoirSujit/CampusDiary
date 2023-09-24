package com.sujitbhoir.campusdiary.pages.Community

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityCommunityPageBinding
import com.sujitbhoir.campusdiary.dataclasses.CommunityData
import com.sujitbhoir.campusdiary.helperclass.DataHandler

class CommunityPage : AppCompatActivity() {

    lateinit var binding : ActivityCommunityPageBinding
    private val TAG = "commumnityPageTAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }

        val comId = intent.getStringExtra("community_id").toString()

        //set
        val db = Firebase.firestore



        db.collection("community").document(comId)
            .get()
            .addOnSuccessListener {

                Log.d(TAG, "data of $comId : ${it.data}")
                if (it != null && it.exists()) {
                    //setting data
                    val communityData = it.toObject(CommunityData::class.java)!!
                    Log.d(TAG, "successful to get community")

                    binding.tvComName.text = communityData.name
                    binding.tvMembercount.text = communityData.members.count().toString()
                    binding.tvComAbout.text = communityData.about

                }
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to get community")
            }


    }

    fun getCommunityData(communityId : String) : CommunityData
    {
        //get user info
        val db = Firebase.firestore
        var communityData : CommunityData = null!!


        db.collection("community").document(communityId)
            .get()
            .addOnSuccessListener {

                Log.d(TAG, "data of $communityId : ${it.data}")
                if (it != null && it.exists()) {
                    //setting data
                    communityData = it.toObject(CommunityData::class.java)!!
                    Log.d(TAG, "successful to get community")

                    //set data
                    DataHandler().setCommunityPic(this, communityData.id, binding.ivComIcon)
                    binding.tvComName.text = communityData.name
                    binding.tvMembercount.text = communityData.members.count().toString()
                    binding.tvComAbout.text = communityData.about





                }
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to get community")
            }

        return communityData
    }
}