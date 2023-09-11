package com.sujitbhoir.campusdiary.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.apphosting.datastore.testing.DatastoreTestTrace.FirestoreV1Action
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.MainActivity
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityForgetPasswordBinding
import org.checkerframework.checker.index.qual.LengthOf

class ForgetPassword : AppCompatActivity() {

    private lateinit var binding: ActivityForgetPasswordBinding
    private lateinit var auth: FirebaseAuth
    private val TAG = "fpassTAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)

        auth = Firebase.auth

        binding.btnRpass.setOnClickListener {

            auth.sendPasswordResetEmail(binding.etEmail.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Email send successfully")
                        val intent = Intent(this, Login::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        Log.d(TAG, "error was ${task.exception}")
                    }
                }


        }

        setContentView(binding.root)
    }
}