package com.sujitbhoir.campusdiary.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.Profile
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.MainActivity
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityJoinCampusBinding
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import com.sujitbhoir.campusdiary.settings.EditProfile
import com.sujitbhoir.campusdiary.settings.ManageInterests
import io.grpc.ManagedChannelProvider

class JoinCampus : AppCompatActivity() {

    private lateinit var binding : ActivityJoinCampusBinding
    val TAG = "JoinCampusTAG"
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    var username = ""
    var fullname = ""
    var email = ""
    var pass = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinCampusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        //get info
        username = intent.getStringExtra("username").toString()
        fullname = intent.getStringExtra("fullName").toString()
        email = intent.getStringExtra("email").toString()
        pass = intent.getStringExtra("pass").toString()
        Log.d(TAG, "userdata = $username, $fullname, $email, $pass")

        //set campus
        val campusAdapter = ArrayAdapter(this, R.layout.dropdown_item, resources.getStringArray(R.array.Campus))
        binding.dpCampus.setAdapter(campusAdapter)

        binding.btnJoin.setOnClickListener {
            registerUser()
        }



    }

    private fun registerUser(){
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val uid = task.result.user!!.uid

                    val userinfo : HashMap<String, String> = hashMapOf(
                        "username" to username,
                        "name" to fullname,
                        "email" to email,
                        "id" to uid,
                        "campus" to binding.dpCampus.text.toString()
                    )

                    db.collection("users")
                        .document(uid)
                        .set(userinfo)
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot added with ID: ${it}")

                            DataHandler().updateUserData(this)

                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                            startActivity(intent)

                            val intent2 = Intent(this, com.sujitbhoir.campusdiary.pages.Profile::class.java)
                            intent2.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                            startActivity(intent2)

                            val intent3 = Intent(this, ManageInterests::class.java)
                            startActivity(intent3)
                            finish()

                        }
                        .addOnFailureListener {
                            Log.w(TAG, "Error adding document", it)
                            Toast.makeText(this, "Something went Wrong", Toast.LENGTH_LONG).show()
                        }




                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()

                }
            }
    }
}