package com.sujitbhoir.campusdiary.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.UsersManager


class UsersListAdapter(val context : Context, val dataSet: ArrayList<UserData>, val customClickListener: (ViewHolder, Int) -> Unit) :
    RecyclerView.Adapter<UsersListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView
        val profilepic : ImageView
        val status : TextView


        init {
            // Define click listener for the ViewHolder's View
            uname = view.findViewById(R.id.tv_uname)
            profilepic = view.findViewById(R.id.iv_profile_pic)
            status = view.findViewById(R.id.tv_status)

        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.contact_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.uname.text = dataSet[position].username
        viewHolder.status.text = dataSet[position].about

        UsersManager(context).setProfilePic(dataSet[position].profilePicId, viewHolder.profilepic)

        // Calling the clickListener sent by the constructor
        viewHolder.itemView.setOnClickListener { customClickListener(viewHolder, position) }


    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    @SuppressLint("NotifyDataSetChanged")
    fun clearList()
    {
        dataSet.clear()
        this.notifyDataSetChanged()
    }

}