package com.sujitbhoir.campusdiary.pages

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.search.SearchBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.adapters.CommunityListAdapter
import com.sujitbhoir.campusdiary.databinding.FragmentExploreBinding
import com.sujitbhoir.campusdiary.dataclasses.CommunityData
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.CommunityManager
import com.sujitbhoir.campusdiary.datahandlers.FirebaseStorageHandler
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import com.sujitbhoir.campusdiary.pages.Community.CommunityPage
import com.sujitbhoir.campusdiary.pages.Community.CreateCommunity


class explore : Fragment() {
    private lateinit var binding : FragmentExploreBinding
    private  val TAG = "exploreTAG"
    lateinit var data : UserData
    lateinit var auth : FirebaseAuth
    private lateinit var  firebaseStorageHandler : FirebaseStorageHandler


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExploreBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        //
        val communityManager  = CommunityManager(container!!.context)
        data = DataHandler.getUserData(container.context)!!
        firebaseStorageHandler = FirebaseStorageHandler(requireContext())
        val db = Firebase.firestore
        auth = Firebase.auth
        val comsData = ArrayList<CommunityData>()
        val comsUData = ArrayList<CommunityData>()

        //set profile pic
        firebaseStorageHandler.setProfilePic( data.profilePicId,
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
                R.id.add -> {
                    val intent = Intent(context?.applicationContext, CreateCommunity::class.java)
                    startActivity(intent)
                    true

                }

                R.id.profile -> {
                    Log.d(TAG, "profile pic pressed")
                    val intent = Intent(context?.applicationContext, Profile::class.java)
                    startActivity(intent)
                    true

                }

                else -> false
            }
        }


        //community
        val recyclerView = binding.recycleViewCommunity
        communityManager.getCommunitiesData {

            recyclerView.layoutManager = LinearLayoutManager(container.context)
            recyclerView.adapter = CommunityListAdapter(container.context, it)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.explore_top_menu, menu)
        super.onCreateOptionsMenu(menu,inflater)
        val search = binding.appBar.menu.getItem(R.id.search)

//        val searchView: SearchView = search.actionView as SearchView
//
//
//        searchView.setOnQueryTextListener(object : OnQueryTextListener() {
//            fun onQueryTextSubmit(query: String?): Boolean {
//                return false
//            }
//
//            fun onQueryTextChange(newText: String?): Boolean {
//                // inside on query text change method we are
//                // calling a method to filter our recycler view.
//                filter(newText)
//                return false
//            }
//        })


    }
}








































































