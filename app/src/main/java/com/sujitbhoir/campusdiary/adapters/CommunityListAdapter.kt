package com.sujitbhoir.campusdiary.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.MainActivity
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.dataclasses.CommunityData
import com.sujitbhoir.campusdiary.datahandlers.CommunityManager
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import com.sujitbhoir.campusdiary.pages.Community.CommunityPage

class CommunityListAdapter(val context : Context, private val dataSet: ArrayList<CommunityData>) :
    RecyclerView.Adapter<CommunityListAdapter.ViewHolder>() {
    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val comname: TextView
        val compic : ImageView
        val comabout : TextView
        val commember : TextView
        val joinbtn : FrameLayout
        val campus : TextView
        val j : Button
        val jd : Button
        val e : Button


        init {
            // Define click listener for the ViewHolder's View
            comname = view.findViewById(R.id.tv_cname)
            compic = view.findViewById(R.id.iv_com_pic)
            comabout = view.findViewById(R.id.tv_comm_about)
            commember = view.findViewById(R.id.tv_members)
            joinbtn = view.findViewById(R.id.joinedit_frame)
            campus = view.findViewById(R.id.campusname)
            j = view.findViewById(R.id.join)
            jd = view.findViewById(R.id.joined)
            e = view.findViewById(R.id.edit)


        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CommunityListAdapter.ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.community_row_item, viewGroup, false)

        return CommunityListAdapter.ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: CommunityListAdapter.ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.comname.text = dataSet[position].name
        viewHolder.comabout.text = dataSet[position].about
        viewHolder.commember.text = "${dataSet[position].members.count()} members"
        viewHolder.campus.text = dataSet[position].campus

        CommunityManager(context).setProfilePic(dataSet[position].communityPicId, viewHolder.compic)

        val it = dataSet[position]

        //join button behaviour
        fun toggle(members : List<String>)
        {
            if (Firebase.auth.currentUser!!.uid  == it.admin)
            {
                viewHolder.j.visibility = View.GONE
                viewHolder.jd.visibility = View.GONE
               viewHolder.e.visibility = View.VISIBLE


            }
            else if (Firebase.auth.currentUser!!.uid in  members)
            {
                viewHolder.j.visibility = View.GONE
                viewHolder.jd.visibility = View.VISIBLE
                viewHolder.e.visibility = View.GONE

            }
            else
            {
                viewHolder.j.visibility = View.VISIBLE
                viewHolder.jd.visibility = View.GONE
                viewHolder.e.visibility = View.GONE

            }
        }
        toggle(it.members)


        viewHolder.joinbtn.setOnClickListener { it2 ->
            if (Firebase.auth.currentUser!!.uid  == it.admin)
            {
                //val intent = context.Intent(this, MainActivity::class.java)
                //startActivity(intent)
            }
            else
            {
                CommunityManager(context).joinCommunity(it.id)
                {
                    toggle(it)
                    viewHolder.commember.text = "${it.count()} members"
                }
            }
        }



        // Calling the clickListener sent by the constructor
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(context, CommunityPage::class.java)
            intent.putExtra("community_id", dataSet[position].id)
            context.startActivity(intent)
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}
