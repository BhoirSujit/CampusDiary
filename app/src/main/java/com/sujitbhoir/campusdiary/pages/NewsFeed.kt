package com.sujitbhoir.campusdiary.pages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.FragmentNewsFeedBinding


class NewsFeed : Fragment() {


    private lateinit var binding : FragmentNewsFeedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewsFeedBinding.inflate(inflater, container, false)
        //



        //
        return binding.root
    }


}