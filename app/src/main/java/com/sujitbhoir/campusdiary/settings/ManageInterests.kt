package com.sujitbhoir.campusdiary.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityManageInterestsBinding

class ManageInterests : AppCompatActivity() {
    private lateinit var binding : ActivityManageInterestsBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityManageInterestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }

        //set tags
        val edutag = arrayOf("Bsc", "Bcom", "BA", "Msc", "Law")
        AddChipsInView(edutag, binding.chipGroupEdu)


        val inttag = arrayOf("Astrology","Writing", "Singing", "Painting", "Drawing","Fitness","NCC","Yoga","Gym","Cooking","Nature","Poetry","Travelling","Dance","Books","Cricket","Coding")
        AddChipsInView(inttag, binding.chipGroupInterest)




        //discard click
        binding.btnDiscard.setOnClickListener {
            finish()
        }

        //update
        binding.btnUpdate.setOnClickListener {

            val checkedChipIds = binding.chipGroupEdu.checkedChipIds
            checkedChipIds.addAll(binding.chipGroupInterest.checkedChipIds)

            val tags = ArrayList<String>()

            for (chip in checkedChipIds)
            {
                   tags.add(findViewById<Chip>(chip).text.toString())
            }

            val ref = Firebase.firestore.collection("users").document(Firebase.auth.currentUser!!.uid)
            val data = hashMapOf<String, List<String>>(
                "interests" to tags.toList()
            )

            ref.set(data, SetOptions.merge())
                .addOnSuccessListener {
                   finish()
                }
                .addOnFailureListener{
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                }


        }



    }

    fun AddChipsInView(chipslist : Array<String>, view : ChipGroup, style : Int = com.google.android.material.R.style.Widget_Material3_Chip_Filter, selectedChips : Array<String> = arrayOf())
    {
        for (chipname in chipslist)
        {
            val chip = Chip(this )
            chip.setChipDrawable(ChipDrawable.createFromAttributes(this, null, 0, style))
            chip.text = chipname
            if (chipname in selectedChips)
            {
                chip.isSelected = true
            }
            view.addView(chip)
        }
    }
}