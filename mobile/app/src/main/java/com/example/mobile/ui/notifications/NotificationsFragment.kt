package com.example.mobile.ui.notifications

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mobile.R
import com.example.mobile.api.RetrofitClient
import com.example.mobile.databinding.FragmentNotificationsBinding
import com.example.mobile.dto.user.NotificationsResponse
import com.example.mobile.ui.notificationCard.NotificationCardFragment
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    val notifications = listOf("Notification 1", "Notification 2", "Notification 3")
    private lateinit var notificationsResponse: List<NotificationsResponse>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(layoutInflater, container, false)
        getNotifications()

        return binding.root
    }

    private fun getNotifications() {
        val sharedPref = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", null) ?: return

        val userApi = RetrofitClient.getInstance().userApi
        userApi.getNotifications(token).enqueue(object : Callback<List<NotificationsResponse>> {
            override fun onResponse(
                call: Call<List<NotificationsResponse>>,
                response: Response<List<NotificationsResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val notifications = response.body()!!

                    val fragmentManager = childFragmentManager
                    val existingFragments = fragmentManager.fragments

                    val transaction = fragmentManager.beginTransaction()
                    for (fragment in existingFragments) {
                        if (fragment is NotificationCardFragment) {
                            transaction.remove(fragment)
                        }
                    }

                    notifications.forEachIndexed { index, notification ->
                        val fragment = NotificationCardFragment().apply {
                            arguments = Bundle().apply {
                                putString("username", notification.fromUser.username)
                                putString("avatarUrl", notification.fromUser.avatarUrl)
                                putString("videoTitle", notification.video.title)
                                putString("videoId", notification.video._id)
                                putString("text", notification.text)
                            }
                        }

                        transaction.add(
                            R.id.notificationCardFragment,
                            fragment,
                            "notification_$index"
                        )
                    }

                    transaction.commit()
                } else {
                    Toasty.error(
                        requireContext(),
                        "Error: ${response.code()}",
                        Toast.LENGTH_SHORT,
                        true
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<NotificationsResponse>>, t: Throwable) {
                Toasty.error(requireContext(), "Failure: ${t.message}", Toast.LENGTH_SHORT, true)
                    .show()
            }
        })
    }

}
