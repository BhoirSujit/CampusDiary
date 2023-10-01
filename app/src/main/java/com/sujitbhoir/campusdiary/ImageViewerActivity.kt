package com.sujitbhoir.campusdiary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.core.net.toUri
import com.bumptech.glide.Glide

class ImageViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)

        val b = findViewById<ImageView>(R.id.back_btn)
        b.setOnClickListener{
            finish()
        }

        val i = findViewById<ImageView>(R.id.img)

        intent.getSerializableExtra("image")
            .let {
            Glide.with(this)
                .load(it)
                .into(i)
        }

    }
}