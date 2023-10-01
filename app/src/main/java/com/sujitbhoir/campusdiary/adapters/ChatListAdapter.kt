package com.sujitbhoir.campusdiary.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Path.Direction
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.dataclasses.MessageData
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.UsersManager
import com.sujitbhoir.campusdiary.adapters.ChatListAdapter
import com.sujitbhoir.campusdiary.helperclass.TimeFormater
import java.lang.Math.abs

class ChatListAdapter(val context : Context) :
    RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {
    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val informationmsg : TextView

        //own
        val own_layout : LinearLayout
        val own_msg : TextView
        val own_media_pic : ImageView
        val own_msg_date : TextView

        //other
        val other_layout : LinearLayout
        val other_msg : TextView
        val other_media_pic : ImageView
        val other_msg_date : TextView
        val other_profile_pic : ImageView



        init {
            // Define click listener for the ViewHolder's View
           informationmsg = view.findViewById(R.id.information_chat)

            //own
            own_layout  = view.findViewById(R.id.own_layout)
            own_msg = view.findViewById(R.id.own_tv_message)
            own_media_pic = view.findViewById(R.id.own_img_pic)
            own_msg_date = view.findViewById(R.id.tv_message_date_own)

            //other
            other_layout = view.findViewById(R.id.othersLayout)
            other_msg = view.findViewById(R.id.other_tv_message)
            other_media_pic = view.findViewById(R.id.other_img_pic)
            other_msg_date = view.findViewById(R.id.tv_message_date_other)
            other_profile_pic = view.findViewById(R.id.other_pic)

        }
    }

    var dataSet = ArrayList<MessageData>()

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(data : ArrayList<MessageData>)
    {
        dataSet = data
        this.notifyDataSetChanged()
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.message_container, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        //first message
        //if new date
        val tf = TimeFormater()

        if (tf.isDiffDay(dataSet[kotlin.math.abs(if (position <= 0 ) 0 else position-1)].time , dataSet[position].time))
        {
            viewHolder.informationmsg.visibility = View.VISIBLE
            viewHolder.informationmsg.text = tf.getFormatedTime(dataSet[position].time)
        }



        if (dataSet[position].sender == Firebase.auth.currentUser!!.uid)
        {
            //owns
            viewHolder.own_layout.visibility = View.VISIBLE
            viewHolder.own_msg.text = dataSet[position].msg
            //if (dataSet[position].img.isNotBlank())

            viewHolder.own_msg_date.text = tf.getHoursMin(dataSet[position].time)
        }

        else
        {
            //others
            viewHolder.other_layout.visibility = View.VISIBLE
            viewHolder.other_msg.text = dataSet[position].msg
            viewHolder.other_msg_date.text = tf.getHoursMin(dataSet[position].time)
        }
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