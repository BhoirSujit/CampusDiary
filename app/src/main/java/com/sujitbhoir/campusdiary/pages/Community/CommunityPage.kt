package com.sujitbhoir.campusdiary.pages.Community

import android.app.Dialog
import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.MainActivity
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.adapters.PostListAdapter
import com.sujitbhoir.campusdiary.bottomsheet.UserBottomSheet
import com.sujitbhoir.campusdiary.databinding.ActivityCommunityPageBinding
import com.sujitbhoir.campusdiary.dataclasses.CommunityData
import com.sujitbhoir.campusdiary.dataclasses.PostData
import com.sujitbhoir.campusdiary.datahandlers.CommunityManager
import com.sujitbhoir.campusdiary.datahandlers.ReportsManager
import com.sujitbhoir.campusdiary.datahandlers.UsersManager
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
                    val intent = Intent(this, EditCommunity::class.java)
                intent.putExtra("comid", comId)
                    startActivity(intent)
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

            //report
            binding.reportUser.setOnClickListener { _ ->
                val dialog =
                    Dialog(this, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog)
                dialog.setContentView(R.layout.report_dialog_box)
                val btnsend = dialog.findViewById<Button>(R.id.btn_send_req)
                val btnclose = dialog.findViewById<Button>(R.id.btn_close)
                val tvreq = dialog.findViewById<TextView>(R.id.tv_request_message)

                btnsend.setOnClickListener { _ ->
                    //report
                    ReportsManager().reportCommunity(
                        it.id,
                        Firebase.auth.currentUser!!.uid,
                        tvreq.text.toString()
                    )

                    Toast.makeText(
                        this,
                        "Thank you for submitting report, we take action as soon as possible",
                        Toast.LENGTH_LONG
                    ).show()
                    dialog.dismiss()

                }
                btnclose.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }

            //load post
            loadPost(it.id)

            UsersManager(this).getUserData(it.admin)
            {userData ->
                binding.contactAdmin.setOnClickListener {
                    val userBottomSheet = UserBottomSheet(userData)
                    userBottomSheet.show(supportFragmentManager, UserBottomSheet.TAG)
                }
            }




        }



    }

    override fun onResume() {
        super.onResume()

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
                    val postListAdapter = PostListAdapter(this)
                    postListAdapter.updateData(postArr, HashMap<String, CommunityData>())
                    recyclerView.adapter = postListAdapter

                }
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }

}