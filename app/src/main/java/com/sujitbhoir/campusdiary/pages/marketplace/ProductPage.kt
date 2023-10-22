package com.sujitbhoir.campusdiary.pages.marketplace

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.text.format.DateFormat
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.MainActivity
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.adapters.ProductsGridAdapter
import com.sujitbhoir.campusdiary.bottomsheet.UserBottomSheet
import com.sujitbhoir.campusdiary.databinding.ActivityProductPageBinding
import com.sujitbhoir.campusdiary.datahandlers.FirebaseFirestoreHandler
import com.sujitbhoir.campusdiary.datahandlers.FirebaseStorageHandler
import com.sujitbhoir.campusdiary.datahandlers.MarketplaceManager
import com.sujitbhoir.campusdiary.datahandlers.ReportsManager
import com.sujitbhoir.campusdiary.datahandlers.UsersManager
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import java.util.Calendar
import java.util.Locale


class ProductPage : AppCompatActivity() {

    private lateinit var binding : ActivityProductPageBinding
    private lateinit var recyclerView : RecyclerView
    private val TAG = "ProductPageTAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recycleViewSuggested
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setOnClickListener {
            finish()
        }

        //getintent
        val productId = intent.getStringExtra("productId")!!

        var whatsappno  : String = ""
        var uploaddate : String = ""
        var sellerId : String = ""


        //set data
        MarketplaceManager(this).getProductData(productId) {
            binding.tvName.text = it.name
            binding.tvPrize.text = "Rs "+it.price
            binding.tvCampus.text = it.campus
            binding.tvDetails.text = it.details
            binding.tvProdQuality.text = it.condition
            binding.tvTimeUpload.text = getDate(it.uploadDate.toLong())
            binding.tvUname.text = it.sellerName
            whatsappno = it.contactWhatsapp
            uploaddate = getDate(it.uploadDate.toLong())
            sellerId = it.sellerId
            UsersManager(this).setProfilePic(it.sellerPic, binding.ivProfilePic)
            MarketplaceManager(this).setCarouselImages(it.images, binding.carousel, lifecycle = lifecycle)
            showSuggested(it.sellerId)


            //report
            binding.reportUser.setOnClickListener { _ ->
                val dialog =
                    Dialog(this, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog)
                dialog.setContentView(R.layout.report_dialog_box)
                val btnsend = dialog.findViewById<Button>(R.id.btn_send_req)
                val btnclose = dialog.findViewById<Button>(R.id.btn_close)
                val tvreq = dialog.findViewById<TextView>(R.id.tv_request_message)

                btnsend.setOnClickListener { _ ->
                    //report
                    ReportsManager().reportProduct(
                        it.id,
                        Firebase.auth.currentUser!!.uid,
                        tvreq.text.toString()
                    )

                    Toast.makeText(
                        this,
                        "Thank you for submitting report, we take action as soon as possible",
                        Toast.LENGTH_LONG
                    ).show()
                    dialog.dismiss()

                }
                btnclose.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }

        }

        binding.btnRequest.setOnClickListener {

            UsersManager(this).getUserData(sellerId)
            {
                val userBottomSheet = UserBottomSheet(it)
                userBottomSheet.show(supportFragmentManager, UserBottomSheet.TAG)
            }


        }

        binding.btnContactWhatsapp.setOnClickListener {
            val contact = "+91 $whatsappno" // use country code with your phone number
            val message = "*${binding.tvName.text}*\n*${binding.tvPrize.text}*\nUploaded at : $uploaddate\n\n${binding.tvRequest.text}"

            val url = "https://api.whatsapp.com/send?phone=$contact Number&text=$message"
            try {
                val pm: PackageManager = packageManager
                pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            } catch (e: PackageManager.NameNotFoundException) {
                Toast.makeText(
                    this,
                    "Whatsapp app not installed in your phone",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }





    }

    private fun showSuggested(sellerId : String)
    {


        MarketplaceManager(this).getProductsData(
                Filter.equalTo("sellerId", sellerId)
        ) {
            Log.d(TAG, "i have $it")
            val productListAdapter = ProductsGridAdapter(this,it)
            recyclerView.adapter = productListAdapter
        }
    }

    private fun getDate(timestamp: Long) :String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp * 1000L
        return DateFormat.format("dd-MM-yyyy",calendar).toString()

    }
}