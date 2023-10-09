package com.sujitbhoir.campusdiary


import android.content.Intent
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.databinding.ActivityMainBinding
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import com.sujitbhoir.campusdiary.pages.Communication
import com.sujitbhoir.campusdiary.pages.Community.CreatePost
import com.sujitbhoir.campusdiary.pages.Home
import com.sujitbhoir.campusdiary.pages.explore
import com.sujitbhoir.campusdiary.pages.marketplace.Marketplace


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val TAG = "MainTAG"



    private val fragmentManager = supportFragmentManager
    private var currentFragment: Fragment? = null
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init data
        DataHandler.updateUserData(this)

        val db = Firebase.firestore
        val auth = Firebase.auth

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.frag_container) as NavHostFragment
        navController = navHostFragment.navController

        setupWithNavController(binding.bottomNavigationView, navController)


        //define view
//        val home: Fragment = Home()
//        val explore: Fragment = explore()
//        val communication: Fragment = Communication()
//        val marketplace: Fragment = Marketplace()
//        val fm: FragmentManager = supportFragmentManager
//        var active = home
//
//        var isFragCreatePost = false
//        var isFragCreateExp = false
//        var isFragCreateCom = false
//        var isFragCreateMar = false


        //fm.beginTransaction().add(R.id.frag_container,home, "1").commit()




        binding.createPost2.setOnClickListener {
            val intent = Intent(this, CreatePost::class.java)
            startActivity(intent)
        }

//        binding.bottomNavigationView.setOnItemSelectedListener {
//            when (it.itemId) {
//                R.id.bottom_nav_create -> {
//                    val intent = Intent(this, CreatePost::class.java)
//                    startActivity(intent)
//                }
//            }
//           true
//        }


        binding.bottomNavigationView.menu.getItem(2).setOnMenuItemClickListener {
            val intent = Intent(this, CreatePost::class.java)
                    startActivity(intent)
            true
        }



//        binding.bottomNavigationView.setOnItemSelectedListener {
//            when (it.itemId)
//            {
//                R.id.home2 -> {
//                    if (!isFragCreatePost) {
//                        fm.beginTransaction().add(R.id.frag_container,home, "1").commit()
//                        isFragCreatePost = true
//                    };
//                    fm.beginTransaction().hide(active).show(home).commit();
//                    active = home
//                }
//                R.id.explore->
//                {
//                    if (!isFragCreateExp) {
//                        fm.beginTransaction().add(R.id.frag_container, explore, "2").hide(explore).commit()
//                        isFragCreateExp = true
//                    };
//                    fm.beginTransaction().hide(active).show(explore).commit();
//                    active = explore
//                }
//                R.id.bottom_nav_create -> {
//                    val intent = Intent(this, CreatePost::class.java)
//                    startActivity(intent)
//
////                    if (supportFragmentManager.fragments.contains(Fexplore))
////                        binding.bottomNavigationView.selectedItemId = R.id.bottom_nav_community
////
////                    else if (supportFragmentManager.fragments.contains(FCommunication))
////                        binding.bottomNavigationView.menu.findItem(R.id.bottom_nav_Communication).isChecked = true
////                    if (supportFragmentManager.fragments.contains(FMarketplace))
////                        binding.bottomNavigationView.menu.findItem(R.id.bottom_nav_marketplace).isChecked = true
////                    else binding.bottomNavigationView.menu.findItem(R.id.bottom_nav_home).isChecked = true
//
//                }
//                R.id.communication -> {
//                    if (!isFragCreateCom) {
//                        fm.beginTransaction().add(R.id.frag_container, communication, "3").hide(communication).commit()
//                        isFragCreateCom = true
//                    }
//                    fm.beginTransaction().hide(active).show(communication).commit();
//                    active = communication
//                }
//                R.id.marketplace -> {
//                    if (!isFragCreateMar) {
//                        fm.beginTransaction().add(R.id.frag_container, marketplace, "4").hide(marketplace).commit()
//                        isFragCreateMar = true
//                    }
//                    fm.beginTransaction().hide(active).show(marketplace).commit();
//                    active = marketplace
//                }
//
//            }
//            true
//        }

        DataHandler.updateUserData(baseContext)

        Firebase.auth.currentUser

    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frag_container,fragment)
            commit()
        }

    private fun replaceFragment(@NonNull fragment: Fragment, @NonNull tag: String) {
        if (fragment != currentFragment) {
            fragmentManager
                .beginTransaction()
                .replace(R.id.frag_container, fragment, tag)
                .commit()
            currentFragment = fragment
        }
    }


}