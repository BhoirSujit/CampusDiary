package com.sujitbhoir.campusdiary.adapters

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.rpc.context.AttributeContext.Auth
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.dataclasses.PostData
import com.sujitbhoir.campusdiary.datahandlers.FirebaseStorageHandler
import com.sujitbhoir.campusdiary.datahandlers.PostsManager
import com.sujitbhoir.campusdiary.helperclass.TimeFormater
import com.sujitbhoir.campusdiary.pages.Communication
import com.sujitbhoir.campusdiary.pages.Community.PostPage
import org.w3c.dom.Text
import java.util.Calendar
import java.util.HashMap
import java.util.Locale

class PostListAdapter(private val context : Context, private val dataSet: ArrayList<PostData>) :
RecyclerView.Adapter<PostListAdapter.ViewHolder>(){

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val comName : TextView

        val comPic : ImageView
        val title : TextView
        val like : ImageView
        val likeCount : TextView
        val imagecontainer : FrameLayout

        init {
            // Define click listener for the ViewHolder's View
            comName = view.findViewById(R.id.tv_cname)

            comPic = view.findViewById(R.id.iv_com_pic)
            title = view.findViewById(R.id.tv_post_title)

            like = view.findViewById(R.id.btn_like)
            likeCount = view.findViewById(R.id.tv_like_count)
            imagecontainer = view.findViewById(R.id.post_image)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_container, parent, false)

        return ViewHolder(view)
    }

    private fun getDate(timestamp: Long) :String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp * 1000L
        return DateFormat.format("dd-MM-yyyy",calendar).toString()

    }

    override fun onBindViewHolder(holder: PostListAdapter.ViewHolder, position: Int) {
        holder.comName.text = "${dataSet[position].communityName} . ${TimeFormater().getFormatedTime(dataSet[position].creationDate.toLong())}"

        holder.title.text = dataSet[position].title
        FirebaseStorageHandler(context).setCommunityPic(dataSet[position].profilePicId, holder.comPic)
        holder.likeCount.text = dataSet[position].likes.size.toString()

        holder.like.isSelected = dataSet[position].likes.contains(Firebase.auth.currentUser!!.uid)


        fun doLike()
        {
            PostsManager(context).likeAPost(dataSet[position].id)
            {
                holder.like.isSelected = it.contains(Firebase.auth.currentUser!!.uid)

                holder.likeCount.text = it.size.toString()
            }
        }

        holder.likeCount.setOnClickListener {
            doLike()
        }

        holder.like.setOnClickListener {
            doLike()
        }
        

        if (dataSet[position].images.isNotEmpty())
        {
            PostsManager(context).setPostPicShapable(dataSet[position].images[0], holder.imagecontainer)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PostPage::class.java)
            intent.putExtra("postid", dataSet[position].id)
            context.startActivity(intent)
        }


    }

    override fun getItemCount() : Int = dataSet.size

}

//class ChatListAdapter(private val context : Context, private val dataSet: ArrayList<Communication.SessionsInfo>) :
//    RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {
//
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val uname: TextView
//        val profilepic : ImageView
//        val message : TextView
//        val lmtime : TextView
//
//        init {
//            // Define click listener for the ViewHolder's View
//            uname = view.findViewById(R.id.tv_uname)
//            profilepic = view.findViewById(R.id.iv_profile_pic)
//            message = view.findViewById(R.id.tv_message)
//            lmtime = view.findViewById(R.id.tv_mtime)
//        }
//    }
//
//    // Create new views (invoked by the layout manager)
//    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
//        // Create a new view, which defines the UI of the list item
//        val view = LayoutInflater.from(viewGroup.context)
//            .inflate(R.layout.chat_row_item, viewGroup, false)
//
//        return ViewHolder(view)
//    }
//
//    // Replace the contents of a view (invoked by the layout manager)
//    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
//
//        // Get element from your dataset at this position and replace the
//        // contents of the view with that element
//        viewHolder.uname.text = dataSet[position].sendername
//        viewHolder.message.text = dataSet[position].lasmes
//        viewHolder.lmtime.text = dataSet[position].lastime
//
//        FirebaseStorageHandler(context).setProfilePic(dataSet[position].members[0], viewHolder.profilepic)
//
//    }
//
//    // Return the size of your dataset (invoked by the layout manager)
//    override fun getItemCount() = dataSet.size
//
//}