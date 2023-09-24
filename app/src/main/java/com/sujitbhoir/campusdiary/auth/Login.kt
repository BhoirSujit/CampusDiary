package com.sujitbhoir.campusdiary.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.annotation.BoolRes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.MainActivity
import com.sujitbhoir.campusdiary.databinding.ActivityLoginBinding
import com.sujitbhoir.campusdiary.helperclass.DataHandler

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = "logTAG"


    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        //login
        binding.btnLogin.setOnClickListener {
            if (validate()) login()
        }

        //register
        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        //forget password
        binding.tvForgetPass.setOnClickListener {
            val intent = Intent(this, ForgetPassword::class.java)
            startActivity(intent)
        }
    }

    private fun validate(): Boolean {

        binding.username.error = null
        binding.password.error = null

        return true
    }

    private fun login() {



        auth.signInWithEmailAndPassword(
            binding.tvUsername.text.toString(),
            binding.tvPassword.text.toString()
        )
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    DataHandler.updateUserData(this)


                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)

                    when ((task.exception as FirebaseAuthException).errorCode)
                    {

                            "ERROR_USER_NOT_FOUND" -> binding.username.error = "Email does not found"
                            "ERROR_WRONG_PASSWORD" -> binding.password.error = "Password are incorrect"
                            else -> Toast.makeText(baseContext, "something went wrong.", Toast.LENGTH_SHORT,).show()
                    }
                }
            }
    }
}