package com.sujitbhoir.campusdiary.settings

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

import com.sujitbhoir.campusdiary.ImageViewerActivity
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityEditProfileBinding
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.FirebaseFirestoreHandler
import com.sujitbhoir.campusdiary.datahandlers.FirebaseStorageHandler
import com.sujitbhoir.campusdiary.datahandlers.UsersManager
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException



class EditProfile : AppCompatActivity() {

    private lateinit var binding : ActivityEditProfileBinding
    private val TAG = "editprofileTAG"

    private lateinit var data : UserData
    private lateinit var db : FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var usersManager: UsersManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialize
        usersManager = UsersManager(this)
        data = usersManager.getMyData()!!
        db = Firebase.firestore
        auth = Firebase.auth

        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }





        //set fields
        binding.tvFname.text = Editable.Factory.getInstance().newEditable(data.name)
        binding.tvUname.text = Editable.Factory.getInstance().newEditable(data.username)
        binding.tvAbout.text = Editable.Factory.getInstance().newEditable(data.about)
        binding.dpGender.text = Editable.Factory.getInstance().newEditable(data.gender)
        binding.tvAge.text = Editable.Factory.getInstance().newEditable(data.age)

        //gender set
        val genderAdapter = ArrayAdapter(this, R.layout.dropdown_item, resources.getStringArray(R.array.Gender))
        binding.dpGender.setAdapter(genderAdapter)


        //set image
        usersManager.setProfilePic(data.profilePicId, binding.profilepic)


        //upload pic
        val resultActivity = registerForActivityResult(ActivityResultContracts.PickVisualMedia())
        {
            if ( it != null)
            {
                val uri = it
                usersManager.uploadProfilePic(auth.currentUser!!.uid,uri) {
                    //save profilepicid
                    Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_LONG).show()
                    usersManager.updateUserData(this)
                    {
                        usersManager.setProfilePic(it, binding.profilepic)
                    }


                }
            }
        }

        binding.btnEditprofilepic.setOnClickListener {
            resultActivity.launch(
                PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    .build()
            )
        }

        binding.btnRemove.setOnClickListener {
            //save profilepicid
            usersManager.removeProfilePic(auth.currentUser!!.uid)
            {
                Toast.makeText(this, "Removed Successfully", Toast.LENGTH_LONG).show()
                usersManager.updateUserData(this)
                {
                    usersManager.setProfilePic("", binding.profilepic)
                }

            }
        }

        //view image
        binding.profilepic.setOnClickListener {
            val intent = Intent(this, ImageViewerActivity::class.java)
            intent.putExtra("image", usersManager.getProfilePicFile(data.profilePicId) )
            startActivity(intent)
        }

        //discard
        binding.btnDiscard.setOnClickListener {
            finish()
        }

        fun update()
        {
            val userinfo : HashMap<String, String> = hashMapOf(
                "username" to binding.tvUname.text.toString(),
                "name" to binding.tvFname.text.toString(),
                "about" to binding.tvAbout.text.toString(),
                "gender" to binding.dpGender.text.toString(),
                "age" to binding.tvAge.text.toString()
            )

            db.collection("users")
                .document(Firebase.auth.currentUser!!.uid)
                .set(userinfo, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot added with ID: ${it}")
                    usersManager.updateUserData(this)
                    {
                        Toast.makeText(baseContext, "Updated Successfully", Toast.LENGTH_LONG).show()
                        finish()
                    }

                }
                .addOnFailureListener {
                    Log.w(TAG, "Error adding document", it)
                }
        }

        //update
        binding.btnUpdate.setOnClickListener {
            binding.btnUpdate.isClickable = false
            if (valid())
            {
                if (data.username == binding.tvUname.text.toString())
                {
                    update()
                }
                else
                {
                    val username = binding.tvUname.text.toString()
                    isUsernameExists(username) { exists ->
                        if (exists) {
                            // The username already exists
                            // Handle the case accordingly (e.g., show an error message)
                            Log.d(TAG, "Username exist")
                            binding.tvUname.error = "Username not available"
                        } else {
                            // The username is available
                            // Proceed with the registration or other desired actions
                            Log.d(TAG, "Username not exist")
                            update()

                        }
                    }
                }




            }


        }

    }

    private fun valid() : Boolean
    {

        binding.tvFname.error = null
        binding.tvUname.error = null

        if (binding.tvFname.text!!.isBlank())
        {
            binding.tvFname.error = "please enter full name"
            return false
        }
        if (binding.tvUname.text!!.isBlank())
        {
            binding.tvUname.error = "username cannot be empty"
            return false
        }

        return  true
    }

    private fun isUsernameExists(username: String, callback: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val usernamesCollection = firestore.collection("users")

        usernamesCollection
            .whereEqualTo("username", binding.tvUname.text.toString())
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "username  and data ${it}")
                Log.d(TAG, "username exist ${it.isEmpty}")
                if (it.isEmpty) {
                    callback(false)
                } else {
                    // Handle the error if necessary
                    callback(true)
                }

            }
            .addOnFailureListener {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
            }
    }
}