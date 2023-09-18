package com.sujitbhoir.campusdiary.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.MainActivity
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityConfirmPasswordBinding

class ConfirmPassword : AppCompatActivity() {

    private lateinit var binding : ActivityConfirmPasswordBinding
    private val TAG = "cpTAG"
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    var username = ""
    var fullname = ""
    var email = ""
    var pass = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //intialize
        binding = ActivityConfirmPasswordBinding.inflate(layoutInflater)
        auth = Firebase.auth
        db = Firebase.firestore

        username = intent.getStringExtra("username").toString()
        fullname = intent.getStringExtra("fullName").toString()
        email = intent.getStringExtra("email").toString()
        Log.d(TAG, "userdata = $username, $fullname, $email")


        binding.btnRegister.setOnClickListener {
            ////password check
            Log.d(TAG, "button clicked")

            binding.password.error = null
            binding.ConfirmPassword.error = null


            pass = binding.tvPass.text.toString()
            val cpass = binding.tvCpass.text.toString()
            if (isPasswordValid(pass)) {
                //check confirm password
                if (pass == cpass) {
                    //go for registration
                    next()
                    //registerUser()
                } else {
                    binding.ConfirmPassword.error = "password dosn't match"
                }
            }

        }
        setContentView(binding.root)
    }

    private fun next()
    {
        val intent = Intent(this, JoinCampus::class.java)
        intent.putExtra("username", username)
        intent.putExtra("fullName", fullname)
        intent.putExtra("email", email)
        intent.putExtra("pass", pass)
        startActivity(intent)
    }

    private fun isPasswordValid(password: String): Boolean {
        // Password should not be empty or null

        Log.d(TAG, "purforming validation")
        if (password.isEmpty()) {
            binding.password.error = "Password cannot be empty"
            return false
        }

        // Check the minimum length requirement (you can adjust this as needed)
        val minimumLength = 8
        if (password.length < minimumLength) {
            binding.password.error = "Password minimum length should be 8 characters"
            return false
        }

        return true
    }
}