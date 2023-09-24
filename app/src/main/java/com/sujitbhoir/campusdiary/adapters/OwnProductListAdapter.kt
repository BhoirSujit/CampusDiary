package com.sujitbhoir.campusdiary.adapters

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firestore.v1.TargetOrBuilder
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.dataclasses.ProductData
import com.sujitbhoir.campusdiary.datahandlers.MarketplaceManager
import com.sujitbhoir.campusdiary.pages.Community.CreateCommunity
import com.sujitbhoir.campusdiary.pages.marketplace.ProductPage
import java.util.Calendar
import java.util.Locale

class OwnProductListAdapter(private val context : Context, private val dataSet: ArrayList<ProductData>) :
    RecyclerView.Adapter<OwnProductListAdapter.ViewHolder>(){

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img : ImageView
        val proName : TextView
        val prodate : TextView
        val removebtn : ImageButton

        init {
            img = view.findViewById(R.id.iv_prod)
            proName = view.findViewById(R.id.tv_product_name)
            prodate = view.findViewById(R.id.tv_product_date)
            removebtn = view.findViewById(R.id.imageButton)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.marketplace_bottom_sheet_item_holder, parent, false)

        return ViewHolder(view)
    }

    private fun getDate(timestamp: Long) :String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp * 1000L
        return DateFormat.format("dd-MM-yyyy",calendar).toString()

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.prodate.text = "${getDate(dataSet[position].uploadDate.toLong())}"
        holder.proName.text = dataSet[position].name

        if (dataSet[position].images.isNotEmpty()) {
            MarketplaceManager(context).setProductPic(dataSet[position].images[0], holder.img)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductPage::class.java)
            intent.putExtra("productId", dataSet[position].id)
            context.startActivity(intent)
        }

        holder.removebtn.setOnClickListener {
            //dialog
            val d = MaterialAlertDialogBuilder(context)
                .setTitle("Do you want to Remove")
                .setMessage("Once you remove you cannot retrieve them back")
                .setPositiveButton("Create"){ a, b ->
                MarketplaceManager(context).removeProduct(dataSet[position].id)
                    {
                        Toast.makeText(context, "Removed Successfully", Toast.LENGTH_LONG).show()
                        a.dismiss()
                    }
                }
                .setNegativeButton("Not Now"){a, b ->
                    a.dismiss()
                }
                .show()
        }
    }





    override fun getItemCount() : Int = dataSet.size


}