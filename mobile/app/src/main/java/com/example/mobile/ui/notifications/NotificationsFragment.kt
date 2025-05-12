package com.example.mobile.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.mobile.R
import com.example.mobile.databinding.FragmentNotificationCardBinding
import com.example.mobile.databinding.FragmentNotificationsBinding
import com.example.mobile.ui.notificationCard.NotificationCardFragment

class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    val unreadNotifications = listOf("Notification 1", "Notification 2", "Notification 3")
    val readNotifications = listOf("Notification 1", "Notification 2", "Notification 3")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(layoutInflater, container, false)

        setUpFragment()

        return binding.root
    }

    private fun setUpFragment(){
        val fragmentManager : FragmentManager = parentFragmentManager
        val fragmentTransaction : FragmentTransaction = fragmentManager.beginTransaction()

        unreadNotifications.forEachIndexed { index, notification ->
            val unreadNotificationCardFragment = NotificationCardFragment()

            fragmentTransaction.add(
                R.id.unreadNotificationCardFragment,
                unreadNotificationCardFragment,
                "notification_$index"
            )
        }

        readNotifications.forEachIndexed { index, notification ->
            val readNotificationCardFragment = NotificationCardFragment()

            fragmentTransaction.add(
                R.id.readNotificationCardFragment,
                readNotificationCardFragment,
                "notification_$index"
            )
        }

        fragmentTransaction.commit()
    }

}
