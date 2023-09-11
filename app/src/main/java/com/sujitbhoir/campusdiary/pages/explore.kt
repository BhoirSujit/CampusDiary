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
import com.google.android.material.search.SearchView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.FragmentExploreBinding
import com.sujitbhoir.campusdiary.dataclasses.CommunityData
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import com.sujitbhoir.campusdiary.pages.Community.CommunityPage
import com.sujitbhoir.campusdiary.pages.Community.CreateCommunity


class explore : Fragment() {
    private lateinit var binding : FragmentExploreBinding
    private  val TAG = "exploreTAG"
    lateinit var data : UserData
    lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExploreBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        //
        data = DataHandler().getUserData(requireContext())!!
        val db = Firebase.firestore
        auth = Firebase.auth
        val comsData = ArrayList<CommunityData>()
        val comsUData = ArrayList<CommunityData>()

        //set profile pic
        DataHandler().setProfilePic(requireContext(), data.id,
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

        db.collection("community")
//            .where(
//            Filter.and(
//            Filter.equalTo("receiver", myData.uid ),
//            Filter.equalTo("flag", false)
//        ))
            .get()
            .addOnSuccessListener {
                for (doc in it.documents)
                {
                    val comData = doc.toObject(CommunityData::class.java)!!
                    comsData.add(comData)
                }
                Log.d(TAG, "data1 are : ${it.documents}")
                Log.d(TAG, "data1 are : ${comsData}")

                val recyclerView = binding.recyclerViewCommunity
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = ComListAdapter(requireContext(), comsData)
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }

        //your community
        db.collection("community")
            .whereEqualTo("admin", Firebase.auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                for (doc in it.documents)
                {
                    val comData = doc.toObject(CommunityData::class.java)!!
                    comsUData.add(comData)
                }
                Log.d(TAG, "data1 are : ${it.documents}")
                Log.d(TAG, "data1 are : ${comsUData}")

                val recyclerView = binding.recyclerViewUCommunity
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = ComUListAdapter(requireContext(), comsUData)
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to load")
            }

        //demo purpus
//        binding.button2.setOnClickListener {
//            val intent = Intent(requireContext(), CommunityPage::class.java)
//            intent.putExtra("community_id", "wvpu0KfsLID0cOY4OweL")
//            startActivity(intent)
//        }



        //
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.explore_top_menu, menu)
        super.onCreateOptionsMenu(menu,inflater)
        val search = binding.appBar.menu.getItem(R.id.search)
        binding.searchView.setupWithSearchBar(search as SearchBar)
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


    class ComListAdapter(val context : Context, private val dataSet: ArrayList<CommunityData>) :
        RecyclerView.Adapter<ComListAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val comname: TextView
            val compic : ImageView
            val comabout : TextView
            val commember : TextView
            val joinbtn : Button


            init {
                // Define click listener for the ViewHolder's View
                comname = view.findViewById(R.id.tv_cname)
                compic = view.findViewById(R.id.iv_com_pic)
                comabout = view.findViewById(R.id.tv_comm_about)
                commember = view.findViewById(R.id.tv_members)
                joinbtn = view.findViewById(R.id.btn_com_joinedit)

            }
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.community_row_item, viewGroup, false)

            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.comname.text = dataSet[position].name
            viewHolder.comabout.text = dataSet[position].about
            viewHolder.commember.text = "${dataSet[position].members.count()} members"

            DataHandler().setCommunityPic(context, dataSet[position].id, viewHolder.compic)

            if (Firebase.auth.currentUser!!.uid in  dataSet[position].members)
            {
                viewHolder.joinbtn.text = "Joined"

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



    class ComUListAdapter(val context : Context, private val dataSet: ArrayList<CommunityData>) :
        RecyclerView.Adapter<ComUListAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val comname: TextView
            val compic : ImageView
            val comabout : TextView
            val commember : TextView
            val joinbtn : Button


            init {
                // Define click listener for the ViewHolder's View
                comname = view.findViewById(R.id.tv_cname)
                compic = view.findViewById(R.id.iv_com_pic)
                comabout = view.findViewById(R.id.tv_comm_about)
                commember = view.findViewById(R.id.tv_members)
                joinbtn = view.findViewById(R.id.btn_com_joinedit)

            }
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.community_row_item, viewGroup, false)

            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.comname.text = dataSet[position].name
            viewHolder.comabout.text = dataSet[position].about
            viewHolder.commember.text = "${dataSet[position].members.count()} members"
            viewHolder.joinbtn.text = "Edit"

            DataHandler().setCommunityPic(context, dataSet[position].id, viewHolder.compic)

            // Calling the clickListener sent by the constructor
            viewHolder.itemView.setOnClickListener {
                val intent = Intent(context, CommunityPage::class.java)
                intent.putExtra("community_id", dataSet[position].id)
                context.startActivity(intent)
            }

            viewHolder.joinbtn.setOnClickListener {
                //edit community
            }



        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size

    }
}








































































