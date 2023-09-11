package com.sujitbhoir.campusdiary.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.helperclass.DataHandler


class UserBottomSheet(private val userData: UserData) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.users_bottom_sheet, container, false)

        val tvname = view.findViewById<TextView>(R.id.tv_name)
        val tvuname = view.findViewById<TextView>(R.id.tv_uname)
        val profilepic = view.findViewById<ImageView>(R.id.profilepic)
        val tvabout = view.findViewById<TextView>(R.id.tv_about1)
        val reqbtn = view.findViewById<Button>(R.id.btn_editprofile)

        tvname.text = userData.name
        tvuname.text = userData.username
        tvabout.text = userData.about

        reqbtn.setOnClickListener {



            val dialog = Dialog(requireContext() , com.google.android.material.R.style.ThemeOverlay_Material3_Dialog)
            dialog.setContentView(R.layout.request_dialog_box)
            val btnsend = dialog.findViewById<Button>(R.id.btn_send_req)
            val btnclose = dialog.findViewById<Button>(R.id.btn_close)
            val tvreq = dialog.findViewById<TextView>(R.id.tv_request_message)

            val myData = DataHandler().getUserData(requireContext())!!

            btnsend.setOnClickListener {
                //request
                val reqmes : HashMap<String, Any> = hashMapOf(
                    "sender" to myData.id,
                    "sender_name" to myData.name,
                    "sender_uname" to myData.username,
                    "receiver" to userData.id,
                    "message" to tvreq.text.toString(),
                    "time" to Timestamp.now().toDate().toString(),
                    "flag" to false
                )

                val ref = Firebase.firestore.collection("request")
                ref.document(userData.id+myData.id)
                    .set(reqmes)
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot added with ID: ${it}")
                        Toast.makeText(requireContext(), "Request send successfully", Toast.LENGTH_LONG).show()
                        dialog.dismiss()

                    }
                    .addOnFailureListener {
                        Log.w(TAG, "Error adding document", it)
                    }

            }
            btnclose.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        return view
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}
