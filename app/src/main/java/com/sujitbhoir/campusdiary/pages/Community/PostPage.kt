package com.sujitbhoir.campusdiary.pages.Community

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityPostPageBinding
import com.sujitbhoir.campusdiary.dataclasses.PostData
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
        PostsManager(this).getPostData(postid)
        {
            data = it
            setup()
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
            binding.carousel.visibility = View.GONE
        }

        //set Data
        binding.tvMembers.text = data.authUName
        binding.tvName.text = data.title
        binding.tvLikeCount.text = data.likes.size.toString()
        binding.tvCname.text = data.communityName
        binding.tvdes.text = data.context
        binding.tvDate.text = "posted on ${TimeFormater().getFormatedTime(data.creationDate.toLong())}"



        PostsManager(this).setCarouselImages(data.images, binding.carousel, lifecycle)


    }
}