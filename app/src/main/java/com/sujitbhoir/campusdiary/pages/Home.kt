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
    private  lateinit var  postListAdapter : PostListAdapter
    private var  communityData = HashMap<String, CommunityData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        cont = container!!.context
        postListAdapter = PostListAdapter(cont, ArrayList<PostData>())
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
        AddChipsInView(taglist, binding.chipGroup2)
        Log.d(TAG, "interst are : ${data.interests}")


        //showpost
        binding.recyclePost.layoutManager = LinearLayoutManager(cont)
        binding.recyclePost.adapter = postListAdapter

        loadPost()


        return binding.root
    }

    fun getCommunityPics()
    {

    }

    fun getPost(afterLoad : () -> Unit)
    {
        val db = Firebase.firestore
        val auth = Firebase.auth

        if (postArr == null)
        {
            postArr = ArrayList<PostData>()
            db.collection("posts")
                .get()
                .addOnSuccessListener {
                    Log.d(TAG, "data are : ${it.documents}")

                    for (doc in it.documents)
                    {
                        val pData = doc.toObject(PostData::class.java) as PostData
                        postArr!!.add(pData)
                    }

                    afterLoad()
                }
                .addOnFailureListener {
                    Log.d(TAG, "failed to load")
                }
        }
    }

    fun loadPost()
    {
        if (postArr == null)
        {
            getPost {
                val tmpArr = ArrayList<String>()
                for (pd in postArr!!)
                {
                    tmpArr.add(pd.communityId)
                }
                val s = LinkedHashSet<String>(tmpArr)

                CommunityManager(cont).getCommunitiesData(ArrayList(s)) {
                    Log.d("TAG", "data found : "+it)
                    postListAdapter.updateData(postArr!!, it)
                }

            }
            return
        }
        postListAdapter.updateData(postArr!!, communityData)

    }
}