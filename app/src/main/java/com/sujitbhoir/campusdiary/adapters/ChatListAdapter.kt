package com.sujitbhoir.campusdiary.adapters

import android.R.attr.label
import android.R.attr.text
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.ImageViewerActivity
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.bottomsheet.UserBottomSheet
import com.sujitbhoir.campusdiary.dataclasses.MessageData
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.CommunicationManager
import com.sujitbhoir.campusdiary.datahandlers.UsersManager
import com.sujitbhoir.campusdiary.helperclass.TimeFormater
import com.sujitbhoir.campusdiary.pages.communication.ChatActivity


class ChatListAdapter(val context : Context, val sessionId : String,val requiredUserData : HashMap<String, UserData>) :
    RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {
    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val informationmsg : TextView
        val infoContainer : LinearLayout

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
            infoContainer = view.findViewById(R.id.infocont)

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
    fun updateData(data : ArrayList<MessageData>) {
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
            viewHolder.infoContainer.visibility = View.VISIBLE
            viewHolder.informationmsg.text = tf.getFormatedDate(dataSet[position].time)
        }

        viewHolder.own_layout.visibility = View.GONE
        viewHolder.other_layout.visibility = View.GONE

        if (dataSet[position].sender == Firebase.auth.currentUser!!.uid)
        {
            //owns
            viewHolder.own_layout.visibility = View.VISIBLE
            viewHolder.own_msg.text = dataSet[position].msg
            viewHolder.own_msg_date.text = tf.getHoursMin(dataSet[position].time)


            if (dataSet[position].img.isNotBlank())
            {
                viewHolder.own_media_pic.visibility = View.VISIBLE
                CommunicationManager(context).setChatMedia(dataSet[position].img, viewHolder.own_media_pic)
                viewHolder.own_media_pic.setOnClickListener {
                    val intent = Intent(context, ImageViewerActivity::class.java)
                    intent.putExtra("image", CommunicationManager(context).getChatMediaFile(dataSet[position].img) )
                    context.startActivity(intent)

                }
            }


        }

        else
        {
            //others
            viewHolder.other_layout.visibility = View.VISIBLE
            viewHolder.other_msg.text = dataSet[position].msg
            viewHolder.other_msg_date.text = tf.getHoursMin(dataSet[position].time)

            //set image
            if (requiredUserData.containsKey(dataSet[position].sender))
            {
                UsersManager(context).setProfilePic(requiredUserData[dataSet[position].sender]!!.profilePicId, viewHolder.other_profile_pic)

                viewHolder.other_profile_pic.setOnClickListener {
                    val userBottomSheet = UserBottomSheet(requiredUserData[dataSet[position].sender]!!)
                    userBottomSheet.show((context as ChatActivity).supportFragmentManager, UserBottomSheet.TAG)
                }
            }

            if (dataSet[position].img.isNotBlank())
            {
                viewHolder.other_media_pic.visibility = View.VISIBLE
                CommunicationManager(context).setChatMedia(dataSet[position].img, viewHolder.other_media_pic)
            }

            viewHolder.other_media_pic.setOnClickListener {
                val intent = Intent(context, ImageViewerActivity::class.java)
                intent.putExtra("image", CommunicationManager(context).getChatMediaFile(dataSet[position].img))
                context.startActivity(intent)
            }



        }

        viewHolder.itemView.setOnCreateContextMenuListener { contextMenu, view, contextMenuInfo ->
            contextMenu.add("send at ${TimeFormater().getFormatedTime(dataSet[position].time)}").setOnMenuItemClickListener {
                true
            }
            contextMenu.add("Copy").setOnMenuItemClickListener {
                val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
                val clip = ClipData.newPlainText("message test", dataSet[position].msg)
                clipboard!!.setPrimaryClip(clip)
                true
            }

            if (dataSet[position].sender == Firebase.auth.currentUser!!.uid) {
                contextMenu.add("delete").setOnMenuItemClickListener {
                    CommunicationManager(context).deleteMessage(sessionId, dataSet[position].id)
                    true
                }
            }

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