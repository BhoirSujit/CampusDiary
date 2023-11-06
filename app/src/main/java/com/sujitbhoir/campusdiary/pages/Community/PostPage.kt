package com.sujitbhoir.campusdiary.pages.Community

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Adapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.ImageViewerActivity
import com.sujitbhoir.campusdiary.MainActivity
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.adapters.CarouselAdapter
import com.sujitbhoir.campusdiary.databinding.ActivityPostPageBinding
import com.sujitbhoir.campusdiary.dataclasses.PostData
import com.sujitbhoir.campusdiary.datahandlers.CommunityManager
import com.sujitbhoir.campusdiary.datahandlers.PostsManager
import com.sujitbhoir.campusdiary.datahandlers.ReportsManager
import com.sujitbhoir.campusdiary.helperclass.TimeFormater

class PostPage : AppCompatActivity() {

    private lateinit var binding : ActivityPostPageBinding
    private lateinit var data : PostData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostPageBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val postid = intent.getStringExtra("postid")!!
        val comPicId = intent.getStringExtra("CommunityPicID")

        if (comPicId != "nopic")
            CommunityManager(this).setProfilePic(comPicId!!, binding.ivComPic)

        PostsManager(this).getPostData(postid)
        {
            data = it
            setup()
            CommunityManager(this).getCommunityData(data.communityId ) {
                CommunityManager(this).setProfilePic(it.communityPicId, binding.ivComPic)
                binding.tvMembers.text = "${it.members.size} Members"
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


                binding.btnComJoinedit.setOnClickListener { it2 ->
                    if (Firebase.auth.currentUser!!.uid  == it.admin)
                    {
                        val intent = Intent(this, EditCommunity::class.java)
                        intent.putExtra("comid", data.communityId)
                        startActivity(intent)
                    }
                    else
                    {
                        CommunityManager(this).joinCommunity(it.id)
                        {
                            toggle(it)
                        }
                    }
                }

                binding.constraintLayout.setOnClickListener {_ ->
                    val intent = Intent(this, CommunityPage::class.java)
                    intent.putExtra("community_id", it.id)
                    this.startActivity(intent)
                }

            }
        }





    }


    private fun setup()
    {
        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }

        if (data.images.isEmpty())
        {
            binding.circularRevealCardView.visibility = View.GONE
        }

        //set Data

        binding.tvName.text = data.title
        binding.btnLike.isSelected = data.likes.contains(Firebase.auth.currentUser!!.uid)
        binding.tvLikeCount.text = data.likes.size.toString()
        binding.tvCname.text = data.communityName
        if (data.context.isNotBlank()) binding.tvdes.text = data.context
        binding.tvDate.text = "posted on ${TimeFormater().getFormatedTime(data.creationDate.toLong())}"



        //PostsManager(this).setCarouselImages(data.images, binding.carousel, lifecycle)
        if (data.images.size > 1)
        {
            PostsManager(this).setCarouselView(binding.carouselView, data.images)
        }
        else if (data.images.isNotEmpty())
        {
            PostsManager(this).setPostPicShapable(data.images[0], binding.postImage) {
                val intent = Intent(this, ImageViewerActivity::class.java)
                intent.putExtra("image", it )
                this.startActivity(intent)
            }
        }
        else {

        }







        //like system
        fun doLike()
        {
            PostsManager(this).likeAPost(data.id)
            {
                binding.btnLike.isSelected = it.contains(Firebase.auth.currentUser!!.uid)

                binding.tvLikeCount.text = it.size.toString()
            }
        }

        binding.btnLike.setOnClickListener {
            doLike()
        }

        binding.tvLikeCount.setOnClickListener {
            doLike()
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
                ReportsManager().reportPost(
                    data.id,
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


    }
}