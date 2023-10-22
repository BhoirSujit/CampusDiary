package com.sujitbhoir.campusdiary.pages

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
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
import android.view.inputmethod.EditorInfo
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
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.adapters.RequestListAdapter
import com.sujitbhoir.campusdiary.adapters.UsersListAdapter
import com.sujitbhoir.campusdiary.bottomsheet.UserBottomSheet
import com.sujitbhoir.campusdiary.databinding.ActivityCreateChatBinding
import com.sujitbhoir.campusdiary.dataclasses.ReqData
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.UsersManager
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import com.sujitbhoir.campusdiary.pages.Community.CreateCommunity
import java.io.File
import java.io.FileOutputStream

class CreateChat : AppCompatActivity() {

    private lateinit var binding: ActivityCreateChatBinding
    private val TAG = "createchatTAG"
    val usersData = ArrayList<UserData>()
    val reqsData = ArrayList<ReqData>()
    lateinit var recyclerView: RecyclerView
    var usersListAdapter : UsersListAdapter? = null
    var requestListAdapter : RequestListAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recycleView
        recyclerView.layoutManager = LinearLayoutManager(this)

        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }

        //initialize
        fun checkforreq()
        {
            Firebase.firestore.collection("requests").where(Filter.and(
                Filter.equalTo("receiver", Firebase.auth.currentUser!!.uid ),
                Filter.equalTo("status", "requested")
            )).count().get(AggregateSource.SERVER).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Count fetched successfully
                    val snapshot = task.result
                    Log.d(TAG, "Count: ${snapshot.count}")
                    if (snapshot.count.toInt() != 0 )
                    Toast.makeText(this, "You have ${snapshot.count} requests. please check it out", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(TAG, "Count failed: ", task.getException())
                }
            }
        }

        checkforreq()

        val auth = Firebase.auth


        binding.sugchip.isChecked = true
        loadSuggested()


        //changes listener
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if(binding.reqchip.isChecked)
            {
                loadRequest()
            }
            else if (binding.sugchip.isChecked)
            {
                loadSuggested()

            }

        }

        //on click listener
        binding.toolbar1.setOnMenuItemClickListener {
            when (it.itemId) {

                R.id.search -> {
                    Log.d(TAG, "search mode")
                    binding.serachbar.visibility = View.VISIBLE
                    binding.chipGroup.visibility = View.GONE



                    true
                }

                else -> false
            }
        }


        //search bar
        binding.serachtext.setOnEditorActionListener { textView, i, keyEvent ->
            var handled = false
            if (i == EditorInfo.IME_ACTION_SEARCH)
            {
                loadBySearch(textView.text.toString())
                handled = true
            }
            return@setOnEditorActionListener handled
        }

        binding.closeSearchbar.setOnClickListener {
            binding.serachbar.visibility = View.GONE
            binding.chipGroup.visibility = View.VISIBLE
        }


    }

    private fun loadBySearch(keyword : String)
    {
        requestListAdapter?.clearList()
        val usersData = ArrayList<UserData>()

        val db = Firebase.firestore
        //suggest //get data
        db.collection("users")
            .orderBy("name")
            .startAt(keyword).endAt(keyword + "\uf8ff")

            .get()
            .addOnSuccessListener {
                //parse data
                for (doc in it.documents)
                {
                    val userData = doc.toObject(UserData::class.java)!!
                    if (userData.id != Firebase.auth.currentUser!!.uid)
                        usersData.add(userData)
                }
                Log.d(TAG, "data are : ${it.documents}")
                Log.d(TAG, "data are : ${usersData}")



                usersListAdapter = UsersListAdapter(this, usersData) {v, i ->
                    val userBottomSheet = UserBottomSheet(usersData[i])
                    userBottomSheet.show(supportFragmentManager, UserBottomSheet.TAG)
                }

                recyclerView.adapter = usersListAdapter



            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadSuggested()
    {
        requestListAdapter?.clearList()

        val db = Firebase.firestore
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



                usersListAdapter = UsersListAdapter(this, usersData) {v, i ->
                    val userBottomSheet = UserBottomSheet(usersData[i])
                    userBottomSheet.show(supportFragmentManager, UserBottomSheet.TAG)
                }

                recyclerView.adapter = usersListAdapter



            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadRequest()
    {
        usersListAdapter?.clearList()
                //request

        val db = Firebase.firestore
        val myData = UsersManager(this).getMyData()!!
        val requireUsersIds = ArrayList<String>()

        db.collection("requests").where(Filter.and(
            Filter.equalTo("receiver", Firebase.auth.currentUser!!.uid ),
            Filter.equalTo("status", "requested")
        ))
            .get()
            .addOnSuccessListener {
                for (doc in it.documents)
                {
                    val reqData = doc.toObject(ReqData::class.java)!!
                    reqsData.add(reqData)
                    requireUsersIds.add(reqData.sender)
                }
                Log.d(TAG, "data1 are : ${it.documents}")
                Log.d(TAG, "data1 are : ${reqsData}")

                //get user data
                UsersManager(this).getUsersData(requireUsersIds)
                {
                    requestListAdapter =  RequestListAdapter(this, reqsData, it) {v, i ->

                        //Dialog


                        val btnsend = findViewById<Button>(R.id.btn_send_req)
                        val btnclose = findViewById<Button>(R.id.btn_decline)
                        val btnignore = findViewById<Button>(R.id.btn_ignore_req)
                        val tvreq = findViewById<EditText>(R.id.tv_request_message)



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
                                "membersnames" to listOf(reqsData[i].receiver, myData.name)
                            )

                            ref.document(sessionid)
                                .set(session)
                                .addOnSuccessListener {
                                    Log.d(UserBottomSheet.TAG, "DocumentSnapshot added with ID: ${it}")
                                    db.collection("request").document(sessionid).delete().addOnSuccessListener {
                                        reqsData.remove(reqsData[i])
                                        requestListAdapter?.updateData(reqsData)
                                    }
                                }
                                .addOnFailureListener {
                                    Log.d(UserBottomSheet.TAG, "Error adding document", it)
                                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                                }

                        }
                        btnclose.setOnClickListener {
                            //red flag
                            val ref = Firebase.firestore.collection("requests")
                            val flag : HashMap<String, Any> = hashMapOf(
                                "status" to "decline"
                            )

                            ref.document(reqsData[i].id)
                                .set(flag, SetOptions.merge())
                                .addOnSuccessListener {
                                    Log.d(UserBottomSheet.TAG, "DocumentSnapshot added with ID: ${it}")
                                    reqsData.remove(reqsData[i])
                                    requestListAdapter?.updateData(reqsData)

                                }
                                .addOnFailureListener {
                                    Log.w(UserBottomSheet.TAG, "Error adding document", it)
                                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                                }
                        }

                        btnignore.setOnClickListener {
                            reqsData.remove(reqsData[i])
                            requestListAdapter?.updateData(reqsData)
                        }


                    }

                    recyclerView.adapter = requestListAdapter
                }



            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }

    }
}