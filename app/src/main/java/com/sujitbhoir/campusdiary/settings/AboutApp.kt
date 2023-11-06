package com.sujitbhoir.campusdiary.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityAboutAppBinding


class AboutApp : AppCompatActivity() {

    private lateinit var binding : ActivityAboutAppBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutAppBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }

        helplinks()

    }

    private fun helplinks()
    {
        binding.github.setOnClickListener{
            val httpIntent = Intent(Intent.ACTION_VIEW)
            httpIntent.data = Uri.parse("https://github.com/BhoirSujit")

            startActivity(httpIntent)
        }

        binding.instagram.setOnClickListener {
            val httpIntent = Intent(Intent.ACTION_VIEW)
            httpIntent.data = Uri.parse("https://www.instagram.com/_sujit004/")

            startActivity(httpIntent)
        }
        binding.facebook.setOnClickListener {
            val httpIntent = Intent(Intent.ACTION_VIEW)
            httpIntent.data = Uri.parse("https://www.facebook.com/sujit.bhoir.5070")

            startActivity(httpIntent)
        }
        binding.linkdn.setOnClickListener {
            val httpIntent = Intent(Intent.ACTION_VIEW)
            httpIntent.data = Uri.parse("https://www.linkedin.com/in/sujit-bhoir-92b29621a/")

            startActivity(httpIntent)
        }
        binding.glide.setOnClickListener {
            val httpIntent = Intent(Intent.ACTION_VIEW)
            httpIntent.data = Uri.parse("https://bumptech.github.io/glide/")

            startActivity(httpIntent)
        }
        binding.firebase.setOnClickListener {
            val httpIntent = Intent(Intent.ACTION_VIEW)
            httpIntent.data = Uri.parse("https://firebase.google.com/")

            startActivity(httpIntent)
        }
        binding.gson.setOnClickListener {
            val httpIntent = Intent(Intent.ACTION_VIEW)
            httpIntent.data = Uri.parse("https://github.com/google/gson")

            startActivity(httpIntent)
        }
        binding.storyset.setOnClickListener {
            val httpIntent = Intent(Intent.ACTION_VIEW)
            httpIntent.data = Uri.parse("https://storyset.com/")

            startActivity(httpIntent)
        }
        binding.whynotimg.setOnClickListener {
            val httpIntent = Intent(Intent.ACTION_VIEW)
            httpIntent.data = Uri.parse("https://github.com/ImaginativeShohag/Why-Not-Image-Carousel")

            startActivity(httpIntent)
        }
        binding.m3.setOnClickListener {
            val httpIntent = Intent(Intent.ACTION_VIEW)
            httpIntent.data = Uri.parse("https://m3.material.io/")

            startActivity(httpIntent)
        }
    }

}