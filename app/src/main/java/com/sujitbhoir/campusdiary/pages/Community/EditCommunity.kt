package com.sujitbhoir.campusdiary.pages.Community

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import com.sujitbhoir.campusdiary.ImageViewerActivity
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityEditCommunityBinding
import com.sujitbhoir.campusdiary.dataclasses.CommunityData
import com.sujitbhoir.campusdiary.datahandlers.CommunityManager
import com.sujitbhoir.campusdiary.datahandlers.UsersManager

class EditCommunity : AppCompatActivity() {


    private lateinit var binding : ActivityEditCommunityBinding
    private var data : CommunityData? =null
    private val TAG = "editCommunityTAG"
    private lateinit var manager : CommunityManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)



        //initialize
        val comid = intent.getStringExtra("comid")!!

        manager = CommunityManager(this)
        val db = Firebase.firestore
        val auth = Firebase.auth

        manager.getCommunityData(comid) {
            data = it
            setup(it)
        }


        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }








    }

    val resultActivity = registerForActivityResult(ActivityResultContracts.PickVisualMedia())
    {
        val db = Firebase.firestore
        if ( it != null)
        {
            val uri = it
            manager.uploadCommunityPic(uri, data!!.id) {
                db.collection("community").document(data!!.id).set(
                    hashMapOf(
                        "communityPicId"  to it
                    ), SetOptions.merge()
                ).addOnSuccessListener {_ ->
                    Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_LONG).show()
                    manager.setProfilePic(it, binding.profilepic)
                }

            }
        }
    }


    fun setup(data : CommunityData)
    {

        val manager = CommunityManager(this)
        val db = Firebase.firestore

        //set fields
        binding.tvComName.text = Editable.Factory.getInstance().newEditable(data!!.name)
        binding.tvAbout.text = Editable.Factory.getInstance().newEditable(data!!.about)


        //set image
        manager.setProfilePic(data!!.communityPicId, binding.profilepic)


        //upload pic


        binding.btnEditprofilepic.setOnClickListener {
            resultActivity.launch(
                PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    .build()
            )
        }



        //view image
        binding.profilepic.setOnClickListener {
            val intent = Intent(this, ImageViewerActivity::class.java)
            intent.putExtra("image", manager.getCommunityPicFile(data!!.communityPicId) )
            startActivity(intent)
        }

        //discard
        binding.btnDiscard.setOnClickListener {
            finish()
        }

        //update
        binding.btnUpdate.setOnClickListener {

            if (valid())
            {
                val userinfo : HashMap<String, String> = hashMapOf(

                    "name" to binding.tvComName.text.toString(),
                    "about" to binding.tvAbout.text.toString(),
                )

                db.collection("community")
                    .document(data.id)
                    .set(userinfo, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot added with ID: ${it}")
                        Toast.makeText(baseContext, "Updated Successfully", Toast.LENGTH_LONG).show()
                        finish()

                    }
                    .addOnFailureListener {
                        Log.w(TAG, "Error adding document", it)
                    }
            }

        }

    }

    private fun valid() : Boolean
    {

        binding.tvComName.error = null
        binding.tvAbout.error = null

        if (binding.tvComName.text!!.isBlank())
        {
            binding.tvComName.error = "please enter title"
            return false
        }
        if (binding.tvAbout.text!!.isBlank())
        {
            binding.tvAbout.error = "please enter about"
            return false
        }

        return  true
    }


}