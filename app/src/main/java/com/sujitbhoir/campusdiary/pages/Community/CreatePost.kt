package com.sujitbhoir.campusdiary.pages.Community

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaType
import com.nareshchocha.filepickerlibrary.ui.FilePicker
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityCreatePostBinding
import com.sujitbhoir.campusdiary.dataclasses.CommunityData
import com.sujitbhoir.campusdiary.datahandlers.FirebaseFirestoreHandler
import com.sujitbhoir.campusdiary.datahandlers.FirebaseStorageHandler
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class CreatePost : AppCompatActivity() {

    lateinit var binding : ActivityCreatePostBinding
    lateinit var FBFirestore : FirebaseFirestoreHandler
    private val TAG = "CreatePostTAG"
    private val commsData = ArrayList<CommunityData>()
    private lateinit var filePaths : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FBFirestore = FirebaseFirestoreHandler()

        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }

        //set tags
        val taglist = arrayOf("Astrology","Writing", "Singing", "Painting", "Drawing","Fitness","NCC","Yoga","Gym","Cooking","Nature","Poetry","Travelling","Dance","Books","Cricket","Coding")
        AddChipsInView(taglist, binding.chipGroupTags)


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

        val launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                    // Use the uri to load the image
                    filePaths = it.data?.getStringArrayListExtra(Const.BundleExtras.FILE_PATH_LIST)!!

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

            launcher.launch(
                FilePicker.Builder(this)
                    .pickMediaBuild(PickMediaConfig(
                        mPickMediaType = PickMediaType.ImageOnly,
                        allowMultiple = true,
                    ))
            )

        }

        binding.btnDiscard.setOnClickListener{
            finish()
        }

        //post



        binding.btnUpdate.setOnClickListener{

            val comData : CommunityData = commsData.find {
                it.name ==  binding.dpComm.text.toString()
            }!!

            val checkedChipIds = binding.chipGroupTags.checkedChipIds
            checkedChipIds.addAll(binding.chipGroupTags.checkedChipIds)

            val tags = ArrayList<String>()

            for (chip in checkedChipIds)
            {
                tags.add(findViewById<Chip>(chip).text.toString())
            }


            FBFirestore.uploadPost(
                title = binding.tvTitle.text.toString(),
                context = binding.tvContext.text.toString(),
                authUName = DataHandler.getUserData(this)?.username.toString(),
                communityName = binding.dpComm.text.toString(),
                communityId = comData.id,
                images = filePaths.size.toString(),
                tags = tags
            ) {
                FirebaseStorageHandler(this).uploadPostMedia(filePaths, it)
                {
                    Toast.makeText(this, "Post uploaded successfully", Toast.LENGTH_LONG).show()
                    finish()
                }

            }
        }



    }

    private fun askToCreateCommunity()
    {
        MaterialAlertDialogBuilder(this)
            .setTitle("Don't Have Community")
            .setMessage("For posting post on media you need a community ownership or editorship, If you don't have then create a new community")
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