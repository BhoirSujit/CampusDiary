package com.sujitbhoir.campusdiary.pages.Community

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityCreatePostBinding
import com.sujitbhoir.campusdiary.dataclasses.CommunityData
import com.sujitbhoir.campusdiary.datahandlers.FirebaseFirestoreHandler
import com.sujitbhoir.campusdiary.datahandlers.FirebaseStorageHandler
import com.sujitbhoir.campusdiary.datahandlers.PostsManager
import com.sujitbhoir.campusdiary.datahandlers.UsersManager
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class CreatePost : AppCompatActivity() {

    lateinit var binding : ActivityCreatePostBinding
    lateinit var FBFirestore : FirebaseFirestoreHandler
    private val TAG = "CreatePostTAG"
    private val commsData = ArrayList<CommunityData>()
    private var filePaths  = ArrayList<String>()
    private lateinit var postsManager : PostsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        postsManager = PostsManager(this)

        FBFirestore = FirebaseFirestoreHandler()

        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }

        //set tags
        //val taglist = resources.getStringArray(R.array.CommunityCategory)
        //AddChipsInView(taglist, binding.chipGroupTags)


        FBFirestore.getCommunitiesData(Firebase.auth.currentUser!!.uid)
        {
            if (it.isEmpty())
                askToCreateCommunity()


            val docs = ArrayList<String>()
            for (doc in it)
            {
                docs.add(doc["name"].toString())
                val commData = doc.toObject(CommunityData::class.java)!!
                commsData.add(commData)
            }

            Log.d(TAG, "community are : $docs")

            val commAdapter = ArrayAdapter(this, R.layout.dropdown_item, docs)
            binding.dpComm.setAdapter(commAdapter)
        }

        val pickMultipleMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
                // Callback is invoked after the user selects media items or closes the
                // photo picker.
                if (uris.isNotEmpty()) {
                    filePaths.clear()
                    for (uri in uris)
                    {
                        filePaths.add(uri.toString())

                    }
                    filePaths.reverse()

                    binding.postimage1.visibility = View.GONE

                    val list = mutableListOf<CarouselItem>()


                    for (file in filePaths)
                    {
                        list.add(
                            CarouselItem(
                                imageUrl = file
                            )
                        )
                    }

                    binding.carousel.registerLifecycle(lifecycle)
                    binding.carousel.setData(list)
                }
            }



        binding.btnAdd.setOnClickListener{
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

//            resultActivity.launch(
//
//
//                FilePicker.Builder(this)
//                    .pickMediaBuild(
//                        PickMediaConfig(
//                            mPickMediaType = PickMediaType.ImageOnly,
//                            allowMultiple = true,
//
//                            )
//                    )
//            )

        }

        binding.btnDiscard.setOnClickListener{
            finish()
        }

        //post
        binding.btnUpdate.setOnClickListener{
            binding.btnUpdate.isClickable = false
            if (valid())
            {

                postPost()
            }
            else
            {
                binding.btnUpdate.isClickable = true
            }
        }



    }

    private fun valid() : Boolean
    {

        binding.tvTitle.error = null
        binding.dpComm.error = null

        if (binding.tvTitle.text!!.isBlank())
        {
            binding.tvTitle.error = "please enter title"
            return false
        }
        if (binding.dpComm.text!!.isBlank())
        {
            binding.dpComm.error = "please select community"
            return false
        }

        return  true
    }

    private fun postPost()
    {

        val comData : CommunityData = commsData.find {
            it.name ==  binding.dpComm.text.toString()
        }!!

        //val checkedChipIds = binding.chipGroupTags.checkedChipIds

        val tags = ArrayList<String>()

//        for (chip in checkedChipIds)
//        {
//            tags.add(findViewById<Chip>(chip).text.toString())
//        }

        postsManager.uploadPost(
            title = binding.tvTitle.text.toString(),
            context = binding.tvContext.text.toString(),
            authUName = UsersManager(this).getMyData()!!.username,
            authId = UsersManager(this).getMyData()!!.id,
            communityName = binding.dpComm.text.toString(),
            campus = comData.campus,
            communityId = comData.id,
            editors = Firebase.auth.currentUser!!.uid,
            images = filePaths,
            tags = comData.tags
        ) {
                Toast.makeText(this, "Post uploaded successfully", Toast.LENGTH_LONG).show()
                finish()
        }
    }

    private fun askToCreateCommunity()
    {
        MaterialAlertDialogBuilder(this)
            .setTitle("Don't Have Community")
            .setMessage("For posting post on media you need a community ownership or editorship, If you don't have then create a new community")
            .setCancelable(false)
            .setPositiveButton("Create"){ a, b ->
                startActivity( Intent(this, CreateCommunity::class.java))
                finish()

            }
            .setNegativeButton("Not Now"){a, b ->
                finish()
            }
            .show()
    }

    fun AddChipsInView(chipslist : Array<String>, view : ChipGroup, style : Int = com.google.android.material.R.style.Widget_Material3_Chip_Filter)
    {
        for (chipname in chipslist)
        {
            val chip = Chip(this )
            chip.setChipDrawable(ChipDrawable.createFromAttributes(this, null, 0, style))
            chip.text = chipname
            view.addView(chip)
        }
    }
}