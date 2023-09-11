package com.sujitbhoir.campusdiary.pages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.FragmentUserProfileBinding


class UserProfile : Fragment() {

    private lateinit var binding : FragmentUserProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       binding = FragmentUserProfileBinding.inflate(layoutInflater, container, false)
        //


        //
        return binding.root
    }
}