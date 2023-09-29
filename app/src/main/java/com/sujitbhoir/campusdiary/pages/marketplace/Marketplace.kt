package com.sujitbhoir.campusdiary.pages.marketplace

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.dynamic.SupportFragmentWrapper
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
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
    private lateinit var recyclerView: RecyclerView
    private lateinit var productsGridAdapter: ProductsGridAdapter
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMarketplaceBinding.inflate(inflater, container, false)
        context = container!!.context

        data = DataHandler.getUserData(requireContext())!!
        firebaseStorageHandler = FirebaseStorageHandler(requireContext())
        marketplaceManager = MarketplaceManager(requireContext())

        //add chip
        fun AddChipsInView(chipslist : Array<String>, view : ChipGroup, style : Int = com.google.android.material.R.style.Widget_Material3_Chip_Filter)
        {
            for (chipname in chipslist)
            {
                val chip = Chip(container!!.context)
                chip.setChipDrawable(ChipDrawable.createFromAttributes(container.context, null, 0, style))
                chip.text = chipname
                view.addView(chip)
            }
        }
        val taglist = resources.getStringArray(R.array.MarketplaceCategory)
        AddChipsInView(taglist, binding.chipGroup)



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
        recyclerView = binding.recycleView
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        marketplaceManager.getProductsData()
        {

            productsGridAdapter = ProductsGridAdapter(context,it)
            recyclerView.adapter = productsGridAdapter




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


    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {

        Log.d(TAG, "you menu entered")
        val search = menu.findItem(R.id.search)
        val searchView  = search.actionView as androidx.appcompat.widget.SearchView

        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d(TAG, "you entered")
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.d(TAG, "you  1 entered")
                return false
            }
        })

        activity?.menuInflater?.inflate(R.menu.marketplace_top_menu, menu);
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        Log.d(TAG, "you menu entered")
        val search = menu.findItem(R.id.search)
        val searchView  = search.actionView as androidx.appcompat.widget.SearchView

        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d(TAG, "you entered")
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.d(TAG, "you  1 entered")
                return true
            }
        })

        activity?.menuInflater?.inflate(R.menu.marketplace_top_menu, menu);
    }




}