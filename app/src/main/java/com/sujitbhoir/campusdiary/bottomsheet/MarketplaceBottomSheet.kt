package com.sujitbhoir.campusdiary.bottomsheet

import android.annotation.SuppressLint
import android.content.Context

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.Filter
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.adapters.OwnProductListAdapter
import com.sujitbhoir.campusdiary.datahandlers.MarketplaceManager
import com.sujitbhoir.campusdiary.pages.marketplace.RegisterProduct

class MarketplaceBottomSheet(val sellerID : String)  : BottomSheetDialogFragment() {


    lateinit var context1 : Context
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        context1 = requireContext()
        val view = inflater.inflate(R.layout.marketplace_bottom_sheet, container, false)

        val btn = view.findViewById<Button>(R.id.btn_add)
        btn.setOnClickListener{
            val intent  = Intent(requireContext(), RegisterProduct::class.java)
            startActivity(intent)
        }


        val recyclerView = view.findViewById<RecyclerView>(R.id.recycle_item)
        recyclerView.layoutManager = LinearLayoutManager(context1)
        MarketplaceManager(context1).getProductsData(
            Filter.equalTo("sellerId", sellerID)
        ) {
          recyclerView.adapter = OwnProductListAdapter(context1, it)

        }

        return view
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }

}