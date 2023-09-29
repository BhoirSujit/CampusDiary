package com.sujitbhoir.campusdiary.pages

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.common.base.MoreObjects.ToStringHelper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.auth.ForgetPassword
import com.sujitbhoir.campusdiary.databinding.ActivityProfileBinding
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.FirebaseStorageHandler
import com.sujitbhoir.campusdiary.datahandlers.UsersManager
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import com.sujitbhoir.campusdiary.settings.EditProfile
import com.sujitbhoir.campusdiary.settings.ManageInterests
import java.io.File


class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val TAG = "profileTAG"
    private lateinit var data : UserData
    private lateinit var dataHandler: DataHandler
    private lateinit var  firebaseStorageHandler : FirebaseStorageHandler
    private lateinit var usersManager: UsersManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usersManager = UsersManager(this)

        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }

        dataHandler = DataHandler()
        firebaseStorageHandler = FirebaseStorageHandler(this)
        data = usersManager.getMyData()!!
        val auth = Firebase.auth


        //setup view
        setup()

        //edit profile button
        binding.imageView2.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

        //reset password
        binding.settingResetpass.setOnClickListener {
            val intent = Intent(this, ForgetPassword::class.java)
            startActivity(intent)
        }

        //manage interest
        binding.settingManageinterest.setOnClickListener {
            val intent = Intent(this, ManageInterests::class.java)
            startActivity(intent)
        }

        //log out
        binding.settingLogout.setOnClickListener{
            val file = File(baseContext.filesDir,"user_data.json")
            file.delete()
            auth.signOut()

            finishAffinity()
        }

    }

    fun setup()
    {
        //set data
        binding.tvName.text = data.name
        binding.tvUname.text = data.username
        binding.tvAbout1.text = data.about
        binding.campusname.text = data.campus

        //set image
        usersManager.setProfilePic(data.profilePicId, binding.profilepic)
    }

    override fun onResume() {
        super.onResume()
        data = usersManager.getMyData()!!
        setup()
    }


}