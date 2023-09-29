package com.sujitbhoir.campusdiary.pages.Community

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.MainActivity
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.adapters.PostListAdapter
import com.sujitbhoir.campusdiary.databinding.ActivityCommunityPageBinding
import com.sujitbhoir.campusdiary.dataclasses.CommunityData
import com.sujitbhoir.campusdiary.dataclasses.PostData
import com.sujitbhoir.campusdiary.datahandlers.CommunityManager
import com.sujitbhoir.campusdiary.helperclass.DataHandler

class CommunityPage : AppCompatActivity() {

    lateinit var binding : ActivityCommunityPageBinding
    private val TAG = "commumnityPageTAG"

    private lateinit var communityManager: CommunityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        communityManager = CommunityManager(this)

        //creates button
        val joinBtn = Button(this, null,  R.style.Button).setText("Join")
        val joinedBtn = Button(this, null,  R.style.Button_OutlinedButton).setText("Joined")


        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }

        val comId = intent.getStringExtra("community_id").toString()

        communityManager.getCommunityData(comId)
        {

                binding.tvComName.text = it.name
                binding.tvMembercount.text = "${it.members.count()} members"
                binding.tvComAbout.text = it.about
                communityManager.setProfilePic(it.communityPicId, binding.ivComIcon)
            binding.campusname.text = it.campus

            //join button behaviour
            fun toggle(members : List<String>)
            {
                if (Firebase.auth.currentUser!!.uid  == it.admin)
                {
                    binding.join.visibility = View.GONE
                    binding.joined.visibility = View.GONE
                    binding.edit.visibility = View.VISIBLE


                }
                else if (Firebase.auth.currentUser!!.uid in  members)
                {
                    binding.join.visibility = View.GONE
                    binding.joined.visibility = View.VISIBLE
                    binding.edit.visibility = View.GONE

                }
                else
                {
                    binding.join.visibility = View.VISIBLE
                    binding.joined.visibility = View.GONE
                    binding.edit.visibility = View.GONE

                }
            }
            toggle(it.members)


            binding.btnJoinEdit.setOnClickListener { it2 ->
                if (Firebase.auth.currentUser!!.uid  == it.admin)
                {
                    val intent = Intent(this, MainActivity::class.java)
                    //startActivity(intent)
                }
                else
                {
                    communityManager.joinCommunity(it.id)
                    {
                       toggle(it)
                        binding.tvMembercount.text = "${it.count()} members"
                    }
                }
            }

            //load post
            loadPost(it.id)



            binding.contactAdmin.setOnClickListener {

            }
        }



    }
    fun loadPost(comid : String)
    {
        val db = Firebase.firestore
        val auth = Firebase.auth

        val postArr = ArrayList<PostData>()

        val recyclerView = binding.recycleviewPost
        recyclerView.isNestedScrollingEnabled = false;
        recyclerView.layoutManager = LinearLayoutManager(this)
        db.collection("posts").whereEqualTo("communityId", comid)
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "data are : ${it.documents}")

                for (doc in it.documents)
                {
                    val pData = doc.toObject(PostData::class.java) as PostData
                    postArr.add(pData)
                    val postListAdapter = PostListAdapter(this, postArr)
                    recyclerView.adapter = postListAdapter

                }
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }

}