package com.sujitbhoir.campusdiary.adapters

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.rpc.context.AttributeContext.Auth
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.dataclasses.CommunityData
import com.sujitbhoir.campusdiary.dataclasses.PostData
import com.sujitbhoir.campusdiary.datahandlers.CommunicationManager
import com.sujitbhoir.campusdiary.datahandlers.CommunityManager
import com.sujitbhoir.campusdiary.datahandlers.FirebaseStorageHandler
import com.sujitbhoir.campusdiary.datahandlers.MarketplaceManager
import com.sujitbhoir.campusdiary.datahandlers.PostsManager
import com.sujitbhoir.campusdiary.datahandlers.ReportsManager
import com.sujitbhoir.campusdiary.datahandlers.UsersManager
import com.sujitbhoir.campusdiary.helperclass.TimeFormater
import com.sujitbhoir.campusdiary.pages.Community.PostPage
import org.imaginativeworld.whynotimagecarousel.utils.setImage
import java.util.Calendar
import java.util.Locale

class PostListAdapter(private val context : Context) :
RecyclerView.Adapter<PostListAdapter.ViewHolder>(){

    private var communitiesData = HashMap<String, CommunityData>()
    private var dataSet = ArrayList<PostData>()

    val TAG = "PostListAdapterTAG"
    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(dataSet: ArrayList<PostData>, requirecommunityData : HashMap<String, CommunityData>)
    {
        this.communitiesData = requirecommunityData
        this.dataSet = dataSet
        this.notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val comName : TextView

        val comPic : ImageView
        val title : TextView
        val like : ImageView
        val likeCount : TextView
        val imagecontainer : FrameLayout
        val optionbtn : ImageView



        init {
            // Define click listener for the ViewHolder's View
            comName = view.findViewById(R.id.tv_cname)
            optionbtn = view.findViewById(R.id.btn_option)

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
        holder.comName.text = "${dataSet[position].communityName} - ${TimeFormater().getFormatedTime(dataSet[position].creationDate.toLong())}"

        holder.title.text = dataSet[position].title
        FirebaseStorageHandler(context).setCommunityPic(dataSet[position].profilePicId, holder.comPic)
        holder.likeCount.text = dataSet[position].likes.size.toString()

        holder.like.isSelected = dataSet[position].likes.contains(Firebase.auth.currentUser!!.uid)

        //Log.d(TAG, "data are" +communitiesData)
        if (communitiesData.containsKey(dataSet[position].communityId))
            CommunityManager(context).setProfilePic( communitiesData[dataSet[position].communityId]!!.communityPicId, holder.comPic)
       


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
            holder.imagecontainer.visibility = View.VISIBLE
            PostsManager(context).setPostPicShapable(dataSet[position].images[0], holder.imagecontainer)
        }
        else
        {
            holder.imagecontainer.visibility = View.GONE
        }


        holder.itemView.setOnClickListener {
            val intent = Intent(context, PostPage::class.java)
            intent.putExtra("postid", dataSet[position].id)
            if (communitiesData.containsKey(dataSet[position].communityId))
            intent.putExtra("CommunityPicID", communitiesData.get(dataSet[position].communityId)!!.communityPicId)
            else intent.putExtra("CommunityPicID","nopic")

            context.startActivity(intent)
        }

        holder.optionbtn.
        setOnCreateContextMenuListener { contextMenu, view, contextMenuInfo ->


            if (dataSet[position].authUName == UsersManager(context).getMyData()!!.username) {
                contextMenu.add("delete").setOnMenuItemClickListener {
                    val d = MaterialAlertDialogBuilder(context)
                        .setTitle("Do you want to Remove")
                        .setMessage("Once you remove you cannot retrieve them back")
                        .setPositiveButton("Remove"){ a, b ->

                            PostsManager(context).deletePost(dataSet[position].id)
                                Toast.makeText(context, "Removed Successfully", Toast.LENGTH_LONG).show()
                                a.dismiss()

                        }
                        .setNegativeButton("Not Now"){a, b ->
                            a.dismiss()
                        }
                        .show()


                    true
                }
            }

            else
            {
                contextMenu.add("report").setOnMenuItemClickListener {

                    val dialog =
                        Dialog(context, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog)
                    dialog.setContentView(R.layout.report_dialog_box)
                    val btnsend = dialog.findViewById<Button>(R.id.btn_send_req)
                    val btnclose = dialog.findViewById<Button>(R.id.btn_close)
                    val tvreq = dialog.findViewById<TextView>(R.id.tv_request_message)

                    btnsend.setOnClickListener { _ ->
                        //report
                        ReportsManager().reportPost(
                            dataSet[position].id,
                            Firebase.auth.currentUser!!.uid,
                            tvreq.text.toString()
                        )

                        Toast.makeText(
                            context,
                            "Thank you for submitting report, we take action as soon as possible",
                            Toast.LENGTH_LONG
                        ).show()
                        dialog.dismiss()

                    }
                    btnclose.setOnClickListener {
                        dialog.dismiss()
                    }

                    dialog.show()

                    true
                }
            }

        }


    }

    override fun getItemCount() : Int = dataSet.size

}
