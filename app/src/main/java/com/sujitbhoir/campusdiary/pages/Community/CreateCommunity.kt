package com.sujitbhoir.campusdiary.pages.Community


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityCreateCommunityBinding
import com.sujitbhoir.campusdiary.datahandlers.CommunityManager
import com.sujitbhoir.campusdiary.datahandlers.FirebaseFirestoreHandler
import com.sujitbhoir.campusdiary.datahandlers.FirebaseStorageHandler


class CreateCommunity : AppCompatActivity() {

    lateinit var binding : ActivityCreateCommunityBinding
    private val TAG = "createcommunityTAG"
    private lateinit var storage : FirebaseStorage
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    var imgUri = Uri.parse("android.resource://my.package.name/" + R.drawable.profile_icon)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialize
        db = Firebase.firestore
        auth = Firebase.auth
        storage = Firebase.storage



        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }

        //set tags
        val taglist = resources.getStringArray(R.array.CommunityCategory)
        AddChipsInView(taglist, binding.chipGrouptags)


        //set campus
        val campusAdapter = ArrayAdapter(this, R.layout.dropdown_item, resources.getStringArray(R.array.Campusplus))
        //campusAdapter.insert("all", 0)
        binding.dpCampus.setAdapter(campusAdapter)

        //upload pic
        val resultActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            if (it.resultCode == RESULT_OK)
            {
                imgUri = it.data?.data!!
                //load pitcher
                Glide.with(this)
                    .load(imgUri)
                    .circleCrop()
                    .into(binding.profilepic)

            }
        }
        binding.btnEditicon.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            resultActivity.launch(intent)
        }

        //discard
        binding.btnDiscard.setOnClickListener {
            finish()
        }

        //btn create
        binding.btnCreate.setOnClickListener {
            createCommunity()
        }


    }

    fun createCommunity()
    {
        val checkedChipIds = binding.chipGrouptags.checkedChipIds
        val tags = ArrayList<String>()

        for (chip in checkedChipIds)
        {
            tags.add(findViewById<Chip>(chip).text.toString())
        }

        CommunityManager(this).createCommunity(
            name = binding.tvFname.text.toString(),
            about = binding.tvAbout.text.toString(),
            campus = binding.dpCampus.text.toString(),
            imgUri = imgUri,
            admin = auth.currentUser!!.uid,
            tags = tags.toList()
            )
        {
            Toast.makeText(this, "Successfully Created Community", Toast.LENGTH_LONG).show()
            finish()
        }
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