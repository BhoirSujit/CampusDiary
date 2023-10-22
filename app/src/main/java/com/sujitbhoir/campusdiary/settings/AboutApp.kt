package com.sujitbhoir.campusdiary.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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


    }
}