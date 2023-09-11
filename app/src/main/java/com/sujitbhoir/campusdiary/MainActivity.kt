package com.sujitbhoir.campusdiary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.databinding.ActivityMainBinding
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import com.sujitbhoir.campusdiary.pages.Communication
import com.sujitbhoir.campusdiary.pages.Community.CreatePost
import com.sujitbhoir.campusdiary.pages.Home
import com.sujitbhoir.campusdiary.pages.Marketplace
import com.sujitbhoir.campusdiary.pages.explore

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val TAG = "MainTAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init data
        DataHandler().updateUserData(this)

        val db = Firebase.firestore
        val auth = Firebase.auth

        //define view
        val Fhome = Home()
        val Fexplore = explore()
        val FCommunication = Communication()
        val FMarketplace = Marketplace()




        //set default
        setCurrentFragment(Fhome)

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId)
            {
                R.id.bottom_nav_home -> setCurrentFragment(Fhome)
                R.id.bottom_nav_community -> setCurrentFragment(Fexplore)
                R.id.bottom_nav_create -> {
                    val intent = Intent(this, CreatePost::class.java)
                    startActivity(intent)

                    if (supportFragmentManager.fragments.contains(Fexplore))
                        binding.bottomNavigationView.selectedItemId = R.id.bottom_nav_community

                    else if (supportFragmentManager.fragments.contains(FCommunication))
                        binding.bottomNavigationView.menu.findItem(R.id.bottom_nav_Communication).isChecked = true
                    if (supportFragmentManager.fragments.contains(FMarketplace))
                        binding.bottomNavigationView.menu.findItem(R.id.bottom_nav_marketplace).isChecked = true
                    else binding.bottomNavigationView.menu.findItem(R.id.bottom_nav_home).isChecked = true

                }
                R.id.bottom_nav_Communication -> setCurrentFragment(FCommunication)
                R.id.bottom_nav_marketplace -> setCurrentFragment(FMarketplace)

            }
            true
        }

        DataHandler().updateUserData(baseContext)

//        //get user info
//        db.collection("users").document(auth.currentUser!!.uid)
//            .get()
//            .addOnSuccessListener {
//
//                Log.d(TAG, "data are : ${it.data}")
//                if (it != null && it.exists()) {
//                    //setting data
//                    val userData = it.toObject(UserData::class.java)
//                    userData?.uid = auth.currentUser!!.uid
//
//                    val gson = Gson()
//                    val json = gson.toJson(userData)
//                    val fileContents = json.toByteArray()
//                    val file = File(baseContext.filesDir,"user_data.json")
//                    try {
//                        FileOutputStream(file).use {
//                            it.write(fileContents)
//                        }
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//
//                    Log.d(TAG, "main TAG , fname : ${userData}")
//                }
//            }
//            .addOnFailureListener {
//                Log.d(TAG, "failed to load")
//            }




    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frag_container,fragment)
            commit()
        }
}