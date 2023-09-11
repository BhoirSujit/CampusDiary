package com.sujitbhoir.campusdiary.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.SignInMethodQueryResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.MainActivity
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityLoginBinding
import com.sujitbhoir.campusdiary.databinding.ActivityRegisterBinding
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CountDownLatch
import kotlin.math.log

class Register : AppCompatActivity() {

    val TAG = "RegTAG"


    private lateinit var binding : ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //next
        binding.btnContinue.setOnClickListener {
            if (validate()) {
                checkingData()
            }
        }

        //sign in
        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)

        }


    }

    private fun checkingData()
    {
        //disable errors
        binding.email.error = null
        binding.name.error = null
        binding.username.error = null


        if (validate()) {

            val email = binding.tvEmail.text.toString()
            isEmailAlreadyInUse(email) { isUsed ->
                if (isUsed) {
                    // The username already exists
                    // Handle the case accordingly (e.g., show an error message)
                    Log.d(TAG, "email exist")
                    binding.email.error = "Email was already used"
                } else {
                    // The username is available
                    // Proceed with the registration or other desired actions
                    Log.d(TAG, "email not exist")
                    val username = binding.tvUsername.text.toString()
                    isUsernameExists(username) { exists ->
                        if (exists) {
                            // The username already exists
                            // Handle the case accordingly (e.g., show an error message)
                            Log.d(TAG, "Username exist")
                            binding.username.error = "Username not available"
                        } else {
                            // The username is available
                            // Proceed with the registration or other desired actions
                            Log.d(TAG, "Username not exist")
                            next()

                        }
                    }
                }
            }
        }
    }

    private fun next()
    {
        val intent = Intent(this, ConfirmPassword::class.java)
        intent.putExtra("username", binding.tvUsername.text.toString())
        intent.putExtra("fullName", binding.tvName.text.toString())
        intent.putExtra("email", binding.tvEmail.text.toString())
        startActivity(intent)
    }

    private fun validate() : Boolean
    {
        if (!isValidEmail(binding.tvEmail.text.toString()))
        {
            binding.email.error = "Email are not well formed"
            return false
        }
        if (!isUsernameValid(binding.tvUsername.text.toString()))
        {
            binding.username.error = "Username can only contain underscore and alphabets"
            return false
        }
        if (binding.tvName.text.isNullOrEmpty()) {
            binding.name.error = "Enter your full name"
            return false
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")

        return emailPattern.matches(email)
    }

    private fun isUsernameValid(text: String): Boolean {
        val textRegex = Regex("^(?!\\d)[a-zA-Z0-9_]+$")
        return text.matches(textRegex)
    }

    private fun isUsernameExists(username: String, callback: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val usernamesCollection = firestore.collection("users")

        usernamesCollection
            .whereEqualTo("username", binding.tvUsername.text.toString())
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

    fun isEmailAlreadyInUse(email: String, onComplete: (Boolean) -> Unit) {
        val auth = FirebaseAuth.getInstance()

        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    val signInMethods = result?.signInMethods

                    // If the email is already in use (i.e., there are sign-in methods associated with it)
                    val isEmailInUse = signInMethods?.isNotEmpty() ?: false
                    onComplete(isEmailInUse)
                } else {
                    // Something went wrong with the task, handle the error here if necessary
                    onComplete(false)
                }
            }
    }
}