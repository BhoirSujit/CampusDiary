package com.sujitbhoir.campusdiary.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.dataclasses.PostData
import com.sujitbhoir.campusdiary.dataclasses.ProductData
import com.sujitbhoir.campusdiary.datahandlers.FirebaseStorageHandler
import com.sujitbhoir.campusdiary.datahandlers.MarketplaceManager
import com.sujitbhoir.campusdiary.pages.marketplace.ProductPage
import org.w3c.dom.Text

class ProductsGridAdapter(private val context : Context, private val dataSet: ArrayList<ProductData>) :
    RecyclerView.Adapter<ProductsGridAdapter.ViewHolder>(){

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img : ImageView
        val proName : TextView
        val proPrize : TextView

        init {
            img = view.findViewById(R.id.img_product)
            proName = view.findViewById(R.id.tv_product_name)
            proPrize = view.findViewById(R.id.tv_product_prize)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_container, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.proPrize.text = "Rs ${dataSet[position].price}"
        holder.proName.text = dataSet[position].name

        if (dataSet[position].images.isNotEmpty()) {
            MarketplaceManager(context).setProductPic(dataSet[position].images[0], holder.img)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductPage::class.java)
            intent.putExtra("productId", dataSet[position].id)
            context.startActivity(intent)
        }
    }





    override fun getItemCount() : Int = dataSet.size

}