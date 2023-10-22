package com.sujitbhoir.campusdiary.pages

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.base.MoreObjects.ToStringHelper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.auth.ForgetPassword
import com.sujitbhoir.campusdiary.databinding.ActivityProfileBinding
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.FirebaseStorageHandler
import com.sujitbhoir.campusdiary.datahandlers.MarketplaceManager
import com.sujitbhoir.campusdiary.datahandlers.UsersManager
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import com.sujitbhoir.campusdiary.settings.AboutApp
import com.sujitbhoir.campusdiary.settings.EditProfile
import com.sujitbhoir.campusdiary.settings.ManageInterests
import com.sujitbhoir.campusdiary.settings.ManageStorage

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val TAG = "profileTAG"
    private lateinit var data : UserData
    private lateinit var dataHandler: DataHandler
    private lateinit var  firebaseStorageHandler : FirebaseStorageHandler
    private lateinit var usersManager: UsersManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usersManager = UsersManager(this)

        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }

        dataHandler = DataHandler()
        firebaseStorageHandler = FirebaseStorageHandler(this)
        data = usersManager.getMyData()!!
        val auth = Firebase.auth


        //setup view
        setup()

        //edit profile button
        binding.imageView2.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

        binding.settingAccount.setOnClickListener{
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

        //reset password
        binding.settingResetpass.setOnClickListener {
            val intent = Intent(this, ForgetPassword::class.java)
            startActivity(intent)
        }

        //manage interest
        binding.settingManageinterest.setOnClickListener {
            val intent = Intent(this, ManageInterests::class.java)
            startActivity(intent)
        }

        //about
        binding.settingAbout.setOnClickListener{
            val intent = Intent(this, AboutApp::class.java)
            startActivity(intent)
        }

        //manage storage
        binding.settingManagestorage.setOnClickListener{
            val intent = Intent(this, ManageStorage::class.java)
            startActivity(intent)
        }

        //share app
        binding.settingShareapp.setOnClickListener {
            //sharing code
            shareAppAsAPK(this)
        }

        //log out
        binding.settingLogout.setOnClickListener{
            val d = MaterialAlertDialogBuilder(this)
                .setTitle("Do you want to log out")
                .setMessage("You need to reopen app once you logout")
                .setPositiveButton("Logout"){ a, b ->
                    val file = File(baseContext.filesDir,"user_data.json")
                    file.delete()
                    auth.signOut()

                    finishAffinity()
                }
                .setNegativeButton("Not Now"){a, b ->
                    a.dismiss()
                }
                .show()


        }

    }

    private fun shareAppAsAPK(context: Context) {
        val app: ApplicationInfo = context.applicationInfo
        val originalApk = app.publicSourceDir
        try {
            //Make new directory in new location
            var tempFile: File = File(context.cacheDir.toString() + "/ExtractedApk")
            //If directory doesn't exists create new
            if (!tempFile.isDirectory) if (!tempFile.mkdirs()) return
            //rename apk file to app name
            tempFile = File(tempFile.path + "/" + getString(app.labelRes).replace(" ", "") + ".apk")
            //If file doesn't exists create new
            if (!tempFile.exists()) {
                if (!tempFile.createNewFile()) {
                    return
                }
            }
            //Copy file to new location
            val inp: InputStream = FileInputStream(originalApk)
            val out: OutputStream = FileOutputStream(tempFile)
            val buf = ByteArray(1024)
            var len: Int
            while (inp.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            inp.close()
            out.close()
            //Open share dialog
            val intent = Intent(Intent.ACTION_SEND)
//MIME type for apk, might not work in bluetooth sahre as it doesn't support apk MIME type

            intent.type = "application/vnd.android.package-archive"
            intent.putExtra(
                Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    context, context.packageName+ ".provider", File(tempFile.path)
                )
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun setup()
    {
        //set data
        binding.tvName.text = data.name
        binding.tvUname.text = data.username
        binding.tvAbout1.text = data.about
        binding.campusname.text = data.campus

        //set image
        usersManager.setProfilePic(data.profilePicId, binding.profilepic)
    }

    override fun onResume() {
        super.onResume()
        data = usersManager.getMyData()!!
        setup()
    }


}