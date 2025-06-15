package com.example.mobile.ui.channel

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mobile.R
import com.example.mobile.api.RetrofitClient
import com.example.mobile.databinding.FragmentChannelBinding
import com.example.mobile.dto.user.ChannelResponse
import com.example.mobile.dto.user.ChannelUser
import com.example.mobile.dto.video.Video
import es.dmoral.toasty.Toasty
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChannelFragment : Fragment() {

    private var _binding: FragmentChannelBinding? = null
    private val binding get() = _binding!!

    private var username: String? = null
    private var isOwnProfile = false
    private var currentUserId: String? = null
    private var currentUsername: String? = null
    private var channelUserId: String? = null
    private var isSubscribed = false

    private val videos = mutableListOf<Video>()
    private lateinit var adapter: ChannelVideoAdapter
    private var page = 1
    private var isLoading = false
    private var hasMore = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        currentUserId = prefs.getString("_id", null)
        currentUsername = prefs.getString("username", null)

        username = arguments?.getString("username")
        isOwnProfile = arguments?.getBoolean("isOwnProfile", false) ?: false

        if (isOwnProfile) {
            username = currentUsername
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChannelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)

        setupRecyclerView()
        username?.let { loadChannelData(it) }

        binding.subscribeButton.setOnClickListener {
            if (isSubscribed) {
                unsubscribeFromChannel()
            } else {
                subscribeToChannel()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ChannelVideoAdapter(videos) { video ->
            Log.d("ChannelFragment", "Navigating to video: ${video._id}")
            val bundle = Bundle().apply {
                putString("videoId", video._id)
            }
            findNavController().navigate(R.id.navigation_video, bundle)
        }

        val layoutManager = LinearLayoutManager(requireContext())
        binding.videosRecyclerView.layoutManager = layoutManager
        binding.videosRecyclerView.adapter = adapter

        binding.videosRecyclerView.isNestedScrollingEnabled = false

        Log.d("ChannelFragment", "RecyclerView setup completed")

        binding.videosRecyclerView.viewTreeObserver.addOnGlobalLayoutListener {
            if (_binding != null && isAdded) {
                Log.d(
                    "ChannelFragment",
                    "RecyclerView layout changed - visible items: ${layoutManager.childCount}"
                )
                Log.d(
                    "ChannelFragment",
                    "RecyclerView height: ${binding.videosRecyclerView.height}"
                )
                Log.d(
                    "ChannelFragment",
                    "RecyclerView measured height: ${binding.videosRecyclerView.measuredHeight}"
                )
            }
        }
    }

    private fun loadChannelData(username: String) {
        Log.d("ChannelFragment", "Loading channel data for username: $username")
        val userApi = RetrofitClient.getInstance().userApi

        userApi.getUserByUsername(username).enqueue(object : Callback<ChannelResponse> {
            override fun onResponse(
                call: Call<ChannelResponse>,
                response: Response<ChannelResponse>
            ) {
                if (!isAdded || _binding == null) return

                Log.d("ChannelFragment", "Channel request URL: ${call.request().url}")
                Log.d("ChannelFragment", "Response code: ${response.code()}")
                Log.d("ChannelFragment", "Response body: ${response.body()}")

                if (response.isSuccessful) {
                    val channelResponse = response.body()
                    channelResponse?.let { data ->
                        Log.d("ChannelFragment", "Channel user: ${data.user.username}")
                        Log.d("ChannelFragment", "Channel videos count: ${data.videos.size}")

                        val sortedVideos = data.videos.sortedByDescending { video ->
                            try {
                                val inputFormat = SimpleDateFormat(
                                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                    Locale.getDefault()
                                )
                                inputFormat.parse(video.createdAt)
                            } catch (e: Exception) {
                                Log.e(
                                    "ChannelFragment",
                                    "Error parsing date: ${video.createdAt}",
                                    e
                                )
                                Date(0)
                            }
                        }

                        Log.d("ChannelFragment", "Videos sorted by date desc")
                        sortedVideos.forEachIndexed { index, video ->
                            Log.d(
                                "ChannelFragment",
                                "Sorted video $index: id=${video._id}, title=${video.title}, date=${video.createdAt}"
                            )
                        }

                        channelUserId = data.user._id
                        binding.channelUsername.text = data.user.username
                        binding.channelSubscribers.text = "${data.user.followersCount} followers"
                        binding.channelSubscriptions.text =
                            "${data.user.subscriptionsCount} subscriptions"

                        Glide.with(requireContext())
                            .load(data.user.avatarUrl)
                            .placeholder(R.drawable.ic_avatar)
                            .into(binding.channelAvatar)

                        videos.clear()
                        videos.addAll(sortedVideos)
                        Log.d(
                            "ChannelFragment",
                            "Videos list updated with sorted videos, size: ${videos.size}"
                        )

                        adapter.notifyDataSetChanged()
                        Log.d("ChannelFragment", "Adapter notified of data change")

                        if (sortedVideos.isEmpty()) {
                            binding.noVideosText.visibility = View.VISIBLE
                            binding.videosRecyclerView.visibility = View.GONE
                        } else {
                            binding.noVideosText.visibility = View.GONE
                            binding.videosRecyclerView.visibility = View.VISIBLE
                        }

                        checkSubscriptionStatus(data.user)
                        updateSubscribeButton()
                    }
                } else {
                    Log.e(
                        "ChannelFragment",
                        "Failed to load channel: ${response.errorBody()?.string()}"
                    )
                }
            }

            override fun onFailure(call: Call<ChannelResponse>, t: Throwable) {
                Log.e("ChannelFragment", "Network error loading channel: ${t.message}", t)
            }
        })
    }

    private fun checkSubscriptionStatus(user: ChannelUser) {
        if (isOwnProfile || currentUserId == user._id) {
            Log.d("ChannelFragment", "This is own profile, hiding subscribe button")
            return
        }

        currentUserId?.let { userId ->
            isSubscribed = user.followers.contains(userId)
            Log.d("ChannelFragment", "Subscription status: $isSubscribed")
        }
    }

    private fun updateSubscribeButton() {
        if (isOwnProfile || currentUserId == channelUserId) {
            binding.subscribeButton.visibility = View.GONE
            Log.d("ChannelFragment", "Subscribe button hidden - own profile")
            return
        }

        binding.subscribeButton.visibility = View.VISIBLE

        if (isSubscribed) {
            binding.subscribeButton.text = "Unsubscribe"
            binding.subscribeButton.setBackgroundResource(R.drawable.button_unsubscribe_shape)
            Log.d("ChannelFragment", "Subscribe button set to Unsubscribe")
        } else {
            binding.subscribeButton.text = "Subscribe"
            binding.subscribeButton.setBackgroundResource(R.drawable.button_shape)
            Log.d("ChannelFragment", "Subscribe button set to Subscribe")
        }
    }

    private fun loadMoreVideos() {
        if (isLoading || !hasMore || username == null) return

        isLoading = true
        page++

        val userApi = RetrofitClient.getInstance().userApi

        userApi.getUserByUsername(username!!).enqueue(object : Callback<ChannelResponse> {
            override fun onResponse(
                call: Call<ChannelResponse>,
                response: Response<ChannelResponse>
            ) {
                isLoading = false

                if (response.isSuccessful && response.body() != null) {
                    val channelResponse = response.body()!!
                    val newVideos = channelResponse.videos

                    val currentVideoIds = videos.map { it._id }.toSet()
                    val videosToAdd = newVideos.filter { !currentVideoIds.contains(it._id) }

                    if (videosToAdd.isNotEmpty()) {
                        val startPosition = videos.size
                        videos.addAll(videosToAdd)
                        adapter.notifyItemRangeInserted(startPosition, videosToAdd.size)
                    }

                    hasMore = videosToAdd.size >= 10
                }
            }

            override fun onFailure(call: Call<ChannelResponse>, t: Throwable) {
                isLoading = false
                page--
            }
        })
    }

    private fun subscribeToChannel() {
        val userId = channelUserId ?: return
        val token = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("token", null) ?: return

        Log.d("ChannelFragment", "Subscribing to channel: $userId")

        RetrofitClient.getInstance().userApi.subscribe(userId, token)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        isSubscribed = true
                        updateSubscribeButton()

                        val currentCount = binding.channelSubscribers.text.toString()
                            .replace(" followers", "").toIntOrNull() ?: 0
                        binding.channelSubscribers.text = "${currentCount + 1} followers"

                        Toasty.success(
                            requireContext(), "Subscribed successfully!",
                            android.widget.Toast.LENGTH_SHORT, true
                        ).show()
                        Log.d("ChannelFragment", "Successfully subscribed")
                    } else {
                        Toasty.error(
                            requireContext(), "Failed to subscribe",
                            android.widget.Toast.LENGTH_SHORT, true
                        ).show()
                        Log.e(
                            "ChannelFragment",
                            "Subscribe failed: ${response.errorBody()?.string()}"
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toasty.error(
                        requireContext(), "Network error",
                        android.widget.Toast.LENGTH_SHORT, true
                    ).show()
                    Log.e("ChannelFragment", "Subscribe network error: ${t.message}")
                }
            })
    }

    private fun unsubscribeFromChannel() {
        val userId = channelUserId ?: return
        val token = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("token", null) ?: return

        Log.d("ChannelFragment", "Unsubscribing from channel: $userId")

        RetrofitClient.getInstance().userApi.unsubscribe(userId, token)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        isSubscribed = false
                        updateSubscribeButton()

                        val currentCount = binding.channelSubscribers.text.toString()
                            .replace(" followers", "").toIntOrNull() ?: 0
                        binding.channelSubscribers.text = "${maxOf(0, currentCount - 1)} followers"

                        Toasty.success(
                            requireContext(), "Unsubscribed successfully!",
                            android.widget.Toast.LENGTH_SHORT, true
                        ).show()
                        Log.d("ChannelFragment", "Successfully unsubscribed")
                    } else {
                        Toasty.error(
                            requireContext(), "Failed to unsubscribe",
                            android.widget.Toast.LENGTH_SHORT, true
                        ).show()
                        Log.e(
                            "ChannelFragment",
                            "Unsubscribe failed: ${response.errorBody()?.string()}"
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toasty.error(
                        requireContext(), "Network error",
                        android.widget.Toast.LENGTH_SHORT, true
                    ).show()
                    Log.e("ChannelFragment", "Unsubscribe network error: ${t.message}")
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}