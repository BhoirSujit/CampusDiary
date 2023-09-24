package com.sujitbhoir.campusdiary.pages.marketplace

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.dynamic.SupportFragmentWrapper
import com.google.android.material.search.SearchBar
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.adapters.ProductsGridAdapter
import com.sujitbhoir.campusdiary.bottomsheet.MarketplaceBottomSheet
import com.sujitbhoir.campusdiary.bottomsheet.UserBottomSheet
import com.sujitbhoir.campusdiary.databinding.FragmentMarketplaceBinding
import com.sujitbhoir.campusdiary.dataclasses.ProductData
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.FirebaseStorageHandler
import com.sujitbhoir.campusdiary.datahandlers.MarketplaceManager
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import com.sujitbhoir.campusdiary.pages.ChatListAdapter
import com.sujitbhoir.campusdiary.pages.Community.CreateCommunity
import com.sujitbhoir.campusdiary.pages.Profile


class Marketplace : Fragment() {

    private lateinit var binding : FragmentMarketplaceBinding
    private val TAG = "marketplaceTAG"
    private lateinit var data : UserData
    private lateinit var  firebaseStorageHandler : FirebaseStorageHandler
    private lateinit var marketplaceManager: MarketplaceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMarketplaceBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        data = DataHandler.getUserData(requireContext())!!
        firebaseStorageHandler = FirebaseStorageHandler(requireContext())
        marketplaceManager = MarketplaceManager(requireContext())

        firebaseStorageHandler.setProfilePic( data.profilePicId,
            object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    // Find the MenuItem by its ID
                    val menuItem = binding.topBar.menu.findItem(R.id.profile)

                    // Set the loaded image as the icon for the menu item
                    menuItem.icon = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle clearing if needed
                }
            })

        //recycle view
        val recyclerView = binding.recycleView
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        marketplaceManager.getProductsData()
        {

            val productListAdapter = ProductsGridAdapter(requireContext(),it)
            recyclerView.adapter = productListAdapter


        }

        //topbar
        binding.topBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.add -> {
                    val userBottomSheet = MarketplaceBottomSheet(data.id)
                    userBottomSheet.show(parentFragmentManager, MarketplaceBottomSheet.TAG)
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





        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.explore_top_menu, menu)
        super.onCreateOptionsMenu(menu,inflater)
        val search = binding.topBar.menu.getItem(R.id.search)
        //binding.searchView.setupWithSearchBar(search as SearchBar)
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