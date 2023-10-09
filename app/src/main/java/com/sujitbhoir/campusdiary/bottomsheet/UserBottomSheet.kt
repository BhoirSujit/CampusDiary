package com.sujitbhoir.campusdiary.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.UsersManager
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
        val reqbtn = view.findViewById<LinearLayout>(R.id.contact_admin)
        val repbtn = view.findViewById<LinearLayout>(R.id.report_user)
        val campusname = view.findViewById<TextView>(R.id.campusname)

        tvname.text = userData.name
        tvuname.text = userData.username
        tvabout.text = userData.about
        campusname.text = userData.campus
        UsersManager(view.context).setProfilePic(userData.profilePicId, profilepic)





        reqbtn.setOnClickListener {
            val dialog = Dialog(requireContext() , com.google.android.material.R.style.ThemeOverlay_Material3_Dialog)
            dialog.setContentView(R.layout.request_dialog_box)
            val btnsend = dialog.findViewById<Button>(R.id.btn_send_req)
            val btnclose = dialog.findViewById<Button>(R.id.btn_close)
            val tvreq = dialog.findViewById<TextView>(R.id.tv_request_message)

            val myData = DataHandler.getUserData(requireContext())!!

            btnsend.setOnClickListener {
                val ref = Firebase.firestore.collection("requests").document()
                val id = ref.id
                //request
                val reqmes : HashMap<String, Any> = hashMapOf(
                    "id" to id,
                    "sender" to myData.id,
                    "receiver" to userData.id,
                    "message" to tvreq.text.toString(),
                    "time" to Timestamp.now(),
                    "status" to "requested"
                )

                ref
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
