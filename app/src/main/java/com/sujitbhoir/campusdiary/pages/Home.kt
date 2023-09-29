package com.sujitbhoir.campusdiary.pages

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.adapters.PostListAdapter
import com.sujitbhoir.campusdiary.databinding.FragmentHomeBinding
import com.sujitbhoir.campusdiary.dataclasses.PostData
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.FirebaseStorageHandler
import com.sujitbhoir.campusdiary.helperclass.DataHandler


class Home : Fragment() {
    private lateinit var binding : FragmentHomeBinding
    private val TAG = "homeTAG"
    private lateinit var cont: Context
    private lateinit var data : UserData
    private lateinit var  firebaseStorageHandler : FirebaseStorageHandler
    private var view : View? = null




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        if (view == null)
        {
        }




        cont = container!!.context
        firebaseStorageHandler = FirebaseStorageHandler(requireContext())

        val db = Firebase.firestore
        val auth = Firebase.auth

        data = DataHandler.getUserData(requireContext())!!

        //set profile pic
        firebaseStorageHandler.setProfilePic( data.profilePicId,
            object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    // Find the MenuItem by its ID
                    val menuItem = binding.appBar.menu.findItem(R.id.profile_pic)

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
                R.id.profile_pic -> {
                    Log.d(TAG, "profile pic pressed")
                    val intent = Intent(context?.applicationContext, Profile::class.java)
                    startActivity(intent)
                    true

                }

                else -> false
            }
        }


        //showpost
        loadPost()


        return binding.root
    }

    fun loadPost()
    {
        val db = Firebase.firestore
        val auth = Firebase.auth

        val postArr = ArrayList<PostData>()

        val recyclerView = binding.recyclePost
        recyclerView.layoutManager = LinearLayoutManager(cont)
        db.collection("posts")
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "data are : ${it.documents}")

                for (doc in it.documents)
                {
                    val pData = doc.toObject(PostData::class.java) as PostData
                    postArr.add(pData)
                    val postListAdapter = PostListAdapter(cont, postArr)
                    recyclerView.adapter = postListAdapter

                }
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }
    }
}