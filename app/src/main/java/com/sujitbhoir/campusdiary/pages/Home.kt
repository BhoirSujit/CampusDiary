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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.adapters.PostListAdapter
import com.sujitbhoir.campusdiary.databinding.FragmentHomeBinding
import com.sujitbhoir.campusdiary.dataclasses.CommunityData
import com.sujitbhoir.campusdiary.dataclasses.PostData
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.CommunityManager
import com.sujitbhoir.campusdiary.datahandlers.FirebaseStorageHandler
import com.sujitbhoir.campusdiary.helperclass.DataHandler


class Home : Fragment() {
    private lateinit var binding : FragmentHomeBinding
    private val TAG = "homeTAG"
    private lateinit var cont: Context
    private lateinit var data : UserData
    private lateinit var  firebaseStorageHandler : FirebaseStorageHandler
    private var view : View? = null

    private var postArr : ArrayList<PostData>? = null
    var reqComData =  HashMap<String, CommunityData>()
    private  lateinit var  postListAdapter : PostListAdapter
    private var  communityData = HashMap<String, CommunityData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        cont = container!!.context
        postListAdapter = PostListAdapter(cont)
        postListAdapter.setHasStableIds(true)

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

        binding.chipall.isChecked = true

        //add interest chip
        fun AddChipsInView(chipslist : List<String>, view : ChipGroup, style : Int = com.google.android.material.R.style.Widget_Material3_Chip_Suggestion)
        {
            for (chipname in chipslist)
            {
                val chip = Chip(container!!.context)
                chip.setChipDrawable(ChipDrawable.createFromAttributes(container.context, null, 0, style))
                chip.text = chipname
                view.addView(chip)
            }
        }
        val taglist = data.interests
        //AddChipsInView(taglist, binding.chipGroup2)
        Log.d(TAG, "interst are : ${data.interests}")


        //showpost
        binding.recyclePost.layoutManager = LinearLayoutManager(cont)
        binding.recyclePost.adapter = postListAdapter

        //changes listener


        binding.chipGroup2.setOnCheckedStateChangeListener { group, checkedIds ->
            if (binding.chipall.isChecked)
            {
                Log.d(TAG, "all chip")
                getPost(db.collection("posts"))
                {
                    loadPost()
                }

            }
            else if (binding.chipfollowing.isChecked)
            {
                Log.d(TAG, "sub chip")
                CommunityManager(cont).getCommunitiesDataBySubscribe {
                    val filters = ArrayList<Filter>()
                    for (data in it)
                    {
                        Log.d(TAG, "filerts for posts are : ${data.id}")
                        filters.add(Filter.equalTo("communityId",data.id))

                    }

                    var f = arrayOfNulls<Filter>(filters.size)
                    f = filters.toArray(f)
                    if (f.isNotEmpty())
                    {
                        getPost(db.collection("posts").where( Filter.or(
                            *f
                        )))
                        {
                            loadPost()
                        }
                    }else
                    {
                        postArr!!.clear()
                        loadPost()
                    }


                }


            }
            else if (binding.chipowncom.isChecked)
            {
                Log.d(TAG, "owncampus chip")
                getPost(db.collection("posts").whereEqualTo("campus", data.campus))
                {
                    loadPost()
                }
            }
            else if (binding.ownposts.isChecked)
            {
                Log.d(TAG, "own chip")
                getPost(db.collection("posts").whereEqualTo("authId", data.id))
                {
                    loadPost()
                }
            }


        }

        loadPost()


        return binding.root
    }


    private val limit : Long = 10


    private fun getPost(ref : Query, afterLoad : () -> Unit)
    {
        val db = Firebase.firestore
        val auth = Firebase.auth


            var templimit = limit

            fun fire()
            {
                binding.loadmorebutton.visibility = View.GONE
                postArr = ArrayList<PostData>()
                reqComData = HashMap<String, CommunityData>()

                ref.limit(templimit)
                    .get()
                    .addOnSuccessListener {
                        Log.d(TAG, "data are : ${it.documents}")


                        for (doc in it.documents)
                        {
                            val pData = doc.toObject(PostData::class.java) as PostData
                            postArr!!.add(pData)
                        }

                        val tmpArr = ArrayList<String>()
                        for (pd in postArr!!)
                        {
                            tmpArr.add(pd.communityId)
                        }
                        val s = LinkedHashSet<String>(tmpArr)

                        CommunityManager(cont).getCommunitiesData(ArrayList(s)) {
                            Log.d("TAG", "data found : "+it)
                            communityData = it

                            Log.d(TAG, "post size and tmp are : ${postArr!!.size} = ${templimit.toInt()}")

                            //show button
                            if (postArr!!.size == templimit.toInt())
                            {
                                binding.loadmorebutton.visibility = View.VISIBLE
                            }

                            afterLoad()

                        }




                    }
                    .addOnFailureListener {
                        Log.d(TAG, "failed to load")
                    }


            }

        fire()



        binding.loadmorebutton.setOnClickListener {
            templimit += limit
            fire()
        }




    }

    fun loadPost()
    {
        val db = Firebase.firestore
        if (postArr == null)
        {
            getPost(db.collection("posts")) {

                    postListAdapter.updateData(postArr!!, communityData)
                if (postArr!!.isEmpty())
                    binding.emptyholder.visibility = View.VISIBLE
                else
                    binding.emptyholder.visibility = View.GONE


            }
            return
        }
        postListAdapter.updateData(postArr!!, communityData)
        if (postArr!!.isEmpty())
            binding.emptyholder.visibility = View.VISIBLE
        else
            binding.emptyholder.visibility = View.GONE

    }
}