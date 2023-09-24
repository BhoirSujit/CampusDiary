package com.sujitbhoir.campusdiary.pages

import android.app.Dialog
import android.content.ClipData
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.common.data.DataHolder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.bottomsheet.UserBottomSheet
import com.sujitbhoir.campusdiary.databinding.ActivityCreateChatBinding
import com.sujitbhoir.campusdiary.dataclasses.ReqData
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import java.io.File
import java.io.FileOutputStream

class CreateChat : AppCompatActivity() {

    private lateinit var binding: ActivityCreateChatBinding
    private val TAG = "createchatTAG"
    val usersData = ArrayList<UserData>()
    val reqsData = ArrayList<ReqData>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }

        //initialize
        val db = Firebase.firestore
        val auth = Firebase.auth
        val myData = DataHandler.getUserData(this)!!


        //request

        db.collection("request").where(Filter.and(
            Filter.equalTo("receiver", myData.id ),
            Filter.equalTo("flag", false)
        ))
            .get()
            .addOnSuccessListener {
                for (doc in it.documents)
                {
                    val reqData = doc.toObject(ReqData::class.java)!!
                    reqsData.add(reqData)
                }
                Log.d(TAG, "data1 are : ${it.documents}")
                Log.d(TAG, "data1 are : ${reqsData}")

                val recyclerView = binding.recycleView
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = ReqListAdapter(this, reqsData) {v, i ->

                    //Dialog

                    val dialog = Dialog(this , com.google.android.material.R.style.ThemeOverlay_Material3_Dialog)
                    dialog.setContentView(R.layout.request_accept_dialog_box)
                    val btnsend = dialog.findViewById<Button>(R.id.btn_send_req)
                    val btnclose = dialog.findViewById<Button>(R.id.btn_close)
                    val btnignore = dialog.findViewById<Button>(R.id.btn_ignore)
                    val tvreq = dialog.findViewById<EditText>(R.id.tv_request_message)



                    btnsend.setOnClickListener {
                        //create session
                        Log.d(TAG, "accpect process")
                        val ref = Firebase.firestore.collection("sessions")
                        val sessionid = ref.document().id
                        val session : HashMap<String, Any> = hashMapOf(
                            "sessionid" to sessionid,
                            "lasmes" to "You can chat now",
                            "lastime" to "",
                            "sender" to myData.id,
                            "sendername" to myData.name,
                            "members" to listOf(reqsData[i].sender, myData.id),
                            "membersnames" to listOf(reqsData[i].receiverName, myData.name)
                        )

                        ref.document(sessionid)
                            .set(session)
                            .addOnSuccessListener {
                                Log.d(UserBottomSheet.TAG, "DocumentSnapshot added with ID: ${it}")
                                db.collection("request").document(myData.id+reqsData[i].sender).delete().addOnSuccessListener {
                                    dialog.dismiss()
                                }
                            }
                            .addOnFailureListener {
                                Log.d(UserBottomSheet.TAG, "Error adding document", it)
                                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                            }

                    }
                    btnclose.setOnClickListener {
                        //red flag
                        val ref = Firebase.firestore.collection("request")
                        val flag : HashMap<String, Any> = hashMapOf(
                            "flag" to true
                        )

                        ref.document(myData.id+reqsData[i].sender)
                            .set(flag, SetOptions.merge())
                            .addOnSuccessListener {
                                Log.d(UserBottomSheet.TAG, "DocumentSnapshot added with ID: ${it}")
                                dialog.dismiss()

                            }
                            .addOnFailureListener {
                                Log.w(UserBottomSheet.TAG, "Error adding document", it)
                                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                            }
                    }

                    btnignore.setOnClickListener {
                        dialog.dismiss()
                    }

                    dialog.show()


                }

            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }




        //suggest //get data
        db.collection("users")
            .whereNotEqualTo("id" , Firebase.auth.currentUser!!.uid)//hide own account

            .get()
            .addOnSuccessListener {
                //parse data
                for (doc in it.documents)
                {
                    val userData = doc.toObject(UserData::class.java)!!
                    usersData.add(userData)
                }
                Log.d(TAG, "data are : ${it.documents}")
                Log.d(TAG, "data are : ${usersData}")


                val recyclerViewsuggest = binding.recycleView2
                recyclerViewsuggest.layoutManager = LinearLayoutManager(this)

                recyclerViewsuggest.adapter = ContactListAdapter(this, usersData) {v, i ->
                    val userBottomSheet = UserBottomSheet(usersData[i])
                    userBottomSheet.show(supportFragmentManager, UserBottomSheet.TAG)
                }



            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }
}

class ContactListAdapter(val context : Context, private val dataSet: ArrayList<UserData>, val customClickListener: (ViewHolder, Int) -> Unit) :
    RecyclerView.Adapter<ContactListAdapter.ViewHolder>() {

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

        DataHandler().setProfilePic(context, dataSet[position].id, viewHolder.profilepic)

        // Calling the clickListener sent by the constructor
        viewHolder.itemView.setOnClickListener { customClickListener(viewHolder, position) }


    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

class ReqListAdapter(val context : Context ,private val dataSet: ArrayList<ReqData>, val customClickListener: (ViewHolder, Int) -> Unit) :
    RecyclerView.Adapter<ReqListAdapter.ViewHolder>() {

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
        viewHolder.uname.text = dataSet[position].senderUName
        viewHolder.status.text = dataSet[position].message

        DataHandler().setProfilePic(context, dataSet[position].sender, viewHolder.profilepic)

        // Calling the clickListener sent by the constructor
        viewHolder.itemView.setOnClickListener { customClickListener(viewHolder, position) }


    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}