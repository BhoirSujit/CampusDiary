package com.sujitbhoir.campusdiary.settings

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityEditProfileBinding
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.firebasehandlers.FirebaseFirestoreHandler
import com.sujitbhoir.campusdiary.firebasehandlers.FirebaseStorageHandler
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException



class EditProfile : AppCompatActivity() {

    private lateinit var binding : ActivityEditProfileBinding
    private val TAG = "editprofileTAG"
    private lateinit var storage : FirebaseStorage
    private lateinit var auth : FirebaseAuth
    private lateinit var data : UserData
    private lateinit var db : FirebaseFirestore

    private lateinit var  firebaseStorageHandler : FirebaseStorageHandler
    private lateinit var firebaseFirestoreHandler: FirebaseFirestoreHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }

        //initialize
        data = DataHandler().getUserData(baseContext)!!
        db = Firebase.firestore
        auth = Firebase.auth
        storage = Firebase.storage
        firebaseStorageHandler = FirebaseStorageHandler(this)
        firebaseFirestoreHandler = FirebaseFirestoreHandler()

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
        firebaseStorageHandler.setProfilePic(data.profilePicId, binding.profilepic)


        //upload pic
        val resultActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            if (it.resultCode == RESULT_OK)
            {
                val uri = it.data?.data!!
                firebaseStorageHandler.uploadProfilePic(uri) {
                    //save profilepicid
                    firebaseFirestoreHandler.updateProfilePicId(auth.currentUser!!.uid, it)
                    {
                        Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_LONG).show()
                        firebaseStorageHandler.setProfilePic(it, binding.profilepic)
                    }
                }
            }
        }
        binding.btnEditprofilepic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            resultActivity.launch(intent)
        }

        //discard
        binding.btnDiscard.setOnClickListener {
            finish()
        }

        //update
        binding.btnUpdate.setOnClickListener {

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
                    DataHandler().updateUserData(baseContext)
                    Toast.makeText(baseContext, "Updated Successfully", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener {
                    Log.w(TAG, "Error adding document", it)
                }
        }

    }

    private fun uploadProfilePic(uri : Uri)
    {
        //compress file
        var bitmap: Bitmap? = null
        try {
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 20, baos)

        val data = baos.toByteArray()

        val timestamp = Timestamp.now().seconds
        val profilepicname = Firebase.auth.currentUser!!.uid

        val ref = storage.reference.child("userspic/${profilepicname}.png")
        ref.putBytes(data)
            .addOnSuccessListener {
                Log.d(TAG, "successfull upload")


                //load pitcher
//                val requestOptions = RequestOptions()
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .circleCrop()
//                    .override(100, 100)
//
//                Glide.with(this)
//                    .load(DataHandler().getProfilePic(this, profilepicname))
//                    .apply(requestOptions)
//                    .into(binding.profilepic)
//                    .onLoadFailed(resources.getDrawable(R.drawable.user))

                DataHandler().setProfilePic(this, profilepicname, binding.profilepic)

                Toast.makeText(baseContext, "Uploaded Successfully", Toast.LENGTH_LONG).show()

            }
            .addOnFailureListener{
                Log.d(TAG, "unsuccessfull upload")
            }
    }

    private fun saveProfilePic(profilepicname : String)
    {
        val ref = storage.reference.child("userspic/${profilepicname}.png")
        val file = File(baseContext.filesDir, "$profilepicname.png")
        ref.getFile(file)
            .addOnSuccessListener {
                Log.d(TAG, "successfull saved")
                
                DataHandler().setProfilePic(this, auth.currentUser!!.uid, binding.profilepic)

            }
            .addOnFailureListener{
                Log.d(TAG, "unsuccessfull saved")
                Toast.makeText(baseContext, "Something went wrong", Toast.LENGTH_LONG).show()
            }
    }


}