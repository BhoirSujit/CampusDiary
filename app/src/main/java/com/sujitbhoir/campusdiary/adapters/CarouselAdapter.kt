package com.sujitbhoir.campusdiary.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sujitbhoir.campusdiary.ImageViewerActivity
import com.sujitbhoir.campusdiary.R
import java.io.File


class CarouselAdapter(val context : Context) : RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {

    private var imagesList = ArrayList<File>()
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val image : ImageView

        init {
            image = itemView.findViewById(R.id.carousel_image_view)
        }

    }

    fun setImage(imagesList : ArrayList<File>)
    {
        this.imagesList = imagesList
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.carousel_view_holder, parent,false))
    }

    override fun getItemCount(): Int {
        return imagesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context)
            .load(imagesList[position])
            .into(holder.image)

        holder.image.setOnClickListener {
            val intent = Intent(context, ImageViewerActivity::class.java)
            intent.putExtra("image", imagesList[position] )
            context.startActivity(intent)

        }
    }

}