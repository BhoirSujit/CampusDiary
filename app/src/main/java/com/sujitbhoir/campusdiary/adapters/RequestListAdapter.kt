package com.sujitbhoir.campusdiary.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.bottomsheet.UserBottomSheet
import com.sujitbhoir.campusdiary.dataclasses.ReqData
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.CommunicationManager
import com.sujitbhoir.campusdiary.datahandlers.NotificationManager
import com.sujitbhoir.campusdiary.datahandlers.UsersManager
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import org.w3c.dom.Text

class RequestListAdapter(val context : Context ,private var dataSet: ArrayList<ReqData>, val requiredUsersData : HashMap<String, UserData>, val customClickListener: (ViewHolder, Int) -> Unit) :
RecyclerView.Adapter<RequestListAdapter.ViewHolder>() {
    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(dataSet: ArrayList<ReqData>)
    {
        this.dataSet = dataSet
        this.notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView
        val profilepic : ImageView
        val status : TextView
        val message: TextView
        val btnIgnore: Button
        val btnDecline:Button
        val btnAccept : Button


        init {
            // Define click listener for the ViewHolder's View
            uname = view.findViewById(R.id.tv_uname)
            profilepic = view.findViewById(R.id.iv_profile_pic)
            status = view.findViewById(R.id.tv_status)
            message = view.findViewById(R.id.tv_message_req)
            btnIgnore = view.findViewById(R.id.btn_ignore_req)
            btnAccept = view.findViewById(R.id.btn_accept)
            btnDecline = view.findViewById(R.id.btn_decline)

        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.request_container, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        //get userdata form list
        if (requiredUsersData.containsKey(dataSet[position].sender))
        {
            val userData = requiredUsersData[dataSet[position].sender]!!
            viewHolder.uname.text = userData.name
            viewHolder.status.text = userData.about
            UsersManager(context).setProfilePic(userData.profilePicId, viewHolder.profilepic)
        }

        viewHolder.message.text= dataSet[position].message
        // Calling the clickListener sent by the constructor


        viewHolder.btnAccept.setOnClickListener {
            CommunicationManager(context).createSession(listOf(Firebase.auth.currentUser!!.uid, dataSet[position].sender)
            ){
                //red flag
                val ref = Firebase.firestore.collection("requests")
                val flag : HashMap<String, Any> = hashMapOf(
                    "status" to "approved"
                )

                ref.document(dataSet[position].id)
                    .set(flag, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(UserBottomSheet.TAG, "DocumentSnapshot added with ID: ${it}")
                        Toast.makeText(context, "Request Accepted", Toast.LENGTH_LONG).show()

                        Log.d("newera","try to get invo : ${dataSet[position].sender}")
                        UsersManager(context).getUserData(dataSet[position].sender){data ->
                            Log.d("newera","invo data : $data")
                            NotificationManager(context).sendAcceptionACK(data.notificationToken, UsersManager(context).getMyData()!!.name, data.id)
                        }
                        dataSet.remove(dataSet[position])
                        updateData(dataSet)

                    }
                    .addOnFailureListener {
                        Log.w(UserBottomSheet.TAG, "Error adding document", it)
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
                    }
            }
        }

        viewHolder.btnDecline.setOnClickListener {
            //red flag
            val ref = Firebase.firestore.collection("requests")
            val flag : HashMap<String, Any> = hashMapOf(
                "status" to "decline"
            )

            ref.document(dataSet[position].id)
                .set(flag, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(UserBottomSheet.TAG, "DocumentSnapshot added with ID: ${it}")
                    dataSet.remove(dataSet[position])
                    updateData(dataSet)

                }
                .addOnFailureListener {
                    Log.w(UserBottomSheet.TAG, "Error adding document", it)
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
                }
        }

        viewHolder.btnIgnore.setOnClickListener {
            dataSet.remove(dataSet[position])
            updateData(dataSet)
        }


        //viewHolder.itemView.setOnClickListener { customClickListener(viewHolder, position) }


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