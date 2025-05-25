package com.example.mobile.ui.notificationCard

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.mobile.R
import com.example.mobile.databinding.FragmentNotificationCardBinding
import androidx.navigation.fragment.findNavController

class NotificationCardFragment : Fragment() {
    private lateinit var binding: FragmentNotificationCardBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationCardBinding.inflate(inflater, container, false)

        val username = arguments?.getString("username") ?: ""
        val avatarUrl = arguments?.getString("avatarUrl") ?: ""
        val videoTitle = arguments?.getString("videoTitle") ?: ""
        val videoId = arguments?.getString("videoId") ?: ""
        val text = arguments?.getString("text") ?: ""

        Glide.with(this)
            .load(avatarUrl)
            .placeholder(R.drawable.ic_avatar)
            .into(binding.avatarImageView)

        binding.usernameTextView.text = username

        val spannable = SpannableString(text)
        val clickablePart = videoTitle
        val startIndex = text.indexOf(clickablePart)

        if (startIndex != -1) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {

                    val bundle = Bundle().apply {
                        putString("videoId", videoId)
                    }

                    val navController = findNavController()
                    navController.navigate(R.id.navigation_video, bundle)
                }
            }
            spannable.setSpan(
                clickableSpan,
                startIndex,
                startIndex + clickablePart.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.messageTextView.text = spannable
            binding.messageTextView.movementMethod = LinkMovementMethod.getInstance()
        } else {
            binding.messageTextView.text = text
        }

        return binding.root
    }
}