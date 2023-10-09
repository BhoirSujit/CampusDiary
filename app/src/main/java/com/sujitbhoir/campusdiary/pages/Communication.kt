package com.sujitbhoir.campusdiary.pages



import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.R

import com.sujitbhoir.campusdiary.databinding.FragmentCommunicationBinding
import com.sujitbhoir.campusdiary.dataclasses.SessionData
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.FirebaseStorageHandler
import com.sujitbhoir.campusdiary.datahandlers.UsersManager
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import com.sujitbhoir.campusdiary.helperclass.TimeFormater
import com.sujitbhoir.campusdiary.pages.communication.ChatActivity


class Communication : Fragment() {
    private lateinit var binding : FragmentCommunicationBinding
    private val TAG = "CommunicationTAG"
    private lateinit var data : UserData
    private lateinit var  firebaseStorageHandler : FirebaseStorageHandler



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunicationBinding.inflate(inflater, container, false)
        data = DataHandler.getUserData(requireContext())!!
        firebaseStorageHandler = FirebaseStorageHandler(requireContext())

        val db = Firebase.firestore
        val auth = Firebase.auth


        //set profile pic
        UsersManager(container!!.context).setProfilePic( data.profilePicId,
            object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    // Find the MenuItem by its ID
                    val menuItem = binding.appBar.menu.findItem(R.id.profile)

                    // Set the loaded image as the icon for the menu item
                    menuItem.icon = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle clearing if needed
                }
            })
        //on click listener
        binding.appBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.profile -> {
                    Log.d(TAG, "profile pic pressed")
                    val intent = Intent(context?.applicationContext, Profile::class.java)
                    startActivity(intent)
                    true

                }

                else -> false
            }
        }

        //float action button
        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(container?.context, CreateChat::class.java)

            startActivity(intent)
        }


        //set data
        //get user info

        val sessionArr = ArrayList<SessionData>()
        val requireUsersIds = ArrayList<String>()

        //
        val recyclerView = binding.recycleView
        recyclerView.layoutManager = LinearLayoutManager(container?.context)
        db.collection("sessions").whereArrayContains("members", auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "data are : ${it.documents}")

                for (doc in it.documents)
                {
                    val sessionData = doc.toObject(SessionData::class.java)!!
                    if (!sessionData.exitmebers.contains( auth.currentUser!!.uid))
                        sessionArr.add(sessionData)
                    requireUsersIds.add(if (sessionData.members[0] == Firebase.auth.currentUser!!.uid) sessionData.members[1] else sessionData.members[0])
                }

                UsersManager(container.context).getUsersData(requireUsersIds)
                {
                    val chatListAdapter = ChatListAdapter(container.context,sessionArr, it)
                    recyclerView.adapter = chatListAdapter
                }


            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }








        //
        return binding.root
    }



}

private class ChatListAdapter(private val context : Context, private val dataSet: ArrayList<SessionData>,val requiredUsersData : HashMap<String, UserData>) :
    RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView
        val profilepic : ImageView
        val message : TextView
        val lmtime : TextView

        init {
            // Define click listener for the ViewHolder's View
            uname = view.findViewById(R.id.tv_uname)
            profilepic = view.findViewById(R.id.iv_profile_pic)
            message = view.findViewById(R.id.tv_message)
            lmtime = view.findViewById(R.id.tv_mtime)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.chat_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        if (requiredUsersData.containsKey(if (dataSet[position].members[0] == Firebase.auth.currentUser!!.uid) dataSet[position].members[1] else dataSet[position].members[0]))
        {
            val userData = requiredUsersData[if (dataSet[position].members[0] == Firebase.auth.currentUser!!.uid) dataSet[position].members[1] else dataSet[position].members[0]]!!
            viewHolder.uname.text = userData.name
            UsersManager(context).setProfilePic(userData.profilePicId, viewHolder.profilepic)
        }

        //viewHolder.uname.text = dataSet[position].sendername
        viewHolder.message.text = dataSet[position].lastmsg
        viewHolder.lmtime.text = TimeFormater().getFormatedTime(dataSet[position].lasttime)
        viewHolder.itemView.setOnClickListener{
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("sessionid", dataSet[position].id)
            context.startActivity(intent)
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}