package com.sujitbhoir.campusdiary.pages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.FragmentCampusMapBinding


class CampusMap : Fragment() {

    private lateinit var binding : FragmentCampusMapBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCampusMapBinding.inflate(inflater, container, false)



        return binding.root
    }


}