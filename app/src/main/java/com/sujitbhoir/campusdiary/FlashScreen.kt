package com.sujitbhoir.campusdiary


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.auth.Login
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FlashScreen : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_flash_screen)

        auth = Firebase.auth




        runBlocking {
            launch {
                delay(3000)


                val currentUser = auth.currentUser
                Log.d("flashact", "current user $currentUser")
                if (currentUser != null)
                {
                    val intent = Intent(this@FlashScreen, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    val intent = Intent(this@FlashScreen, Login::class.java)
                    startActivity(intent)
                    finish()
                }



            }
        }
    }
}