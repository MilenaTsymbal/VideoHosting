package com.example.mobile.ui.notificationCard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.mobile.R
import com.example.mobile.databinding.FragmentNotificationCardBinding
import com.example.mobile.databinding.FragmentNotificationsBinding

class NotificationCardFragment : Fragment() {
    private lateinit var _binding: FragmentNotificationCardBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationCardBinding.inflate(inflater, container, false)

        return _binding.root
    }

}