package com.sujitbhoir.campusdiary.pages.Community

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.MainActivity
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityPostPageBinding
import com.sujitbhoir.campusdiary.dataclasses.PostData
import com.sujitbhoir.campusdiary.datahandlers.CommunityManager
import com.sujitbhoir.campusdiary.datahandlers.PostsManager
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
                        val intent = Intent(this, MainActivity::class.java)
                        //startActivity(intent)
                    }
                    else
                    {
                        CommunityManager(this).joinCommunity(it.id)
                        {
                            toggle(it)
                        }
                    }
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
        binding.tvMembers.text = data.authUName
        binding.tvName.text = data.title
        binding.tvLikeCount.text = data.likes.size.toString()
        binding.tvCname.text = data.communityName
        binding.tvdes.text = data.context
        binding.tvDate.text = "posted on ${TimeFormater().getFormatedTime(data.creationDate.toLong())}"



        PostsManager(this).setCarouselImages(data.images, binding.carousel, lifecycle)

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


    }
}