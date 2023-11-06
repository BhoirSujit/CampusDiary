package com.sujitbhoir.campusdiary


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
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

    fun openNotificationSettings(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        context.startActivity(intent)
    }

    fun isNotificationPermissionEnabled(context: Context): Boolean {
        val notificationManager = NotificationManagerCompat.from(context)
        return notificationManager.areNotificationsEnabled()
    }

    fun showNotificationPermissionDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Notification Permission Required")
        builder.setMessage("Please grant notification permissions to receive important updates.")

        builder.setPositiveButton("Grant Permission") { _, _ ->
            // Open app settings for notification permissions
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

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

        //subscribe to topic
        Firebase.messaging.subscribeToTopic("officialMessage")
        if (!isNotificationPermissionEnabled(this))
        {
            showNotificationPermissionDialog(this)
        }


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