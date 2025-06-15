package com.example.mobile.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.api.RetrofitClient
import com.example.mobile.databinding.FragmentHomeBinding
import com.example.mobile.dto.video.Video
import com.example.mobile.dto.video.VideoListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val videos = mutableListOf<Video>()
    private lateinit var adapter: VideoAdapter
    private var page = 1
    private var isLoading = false
    private var hasMore = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        adapter = VideoAdapter(videos) { video ->
            val bundle = Bundle().apply { putString("videoId", video._id) }
            findNavController().navigate(R.id.navigation_video, bundle)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                val lm = rv.layoutManager as LinearLayoutManager
                val total = lm.itemCount
                val last = lm.findLastVisibleItemPosition()
                
                if (!isLoading && hasMore && last >= total - 3) {
                    Log.d("HomeFragment", "Loading more videos - current page: $page")
                    loadVideos()
                }
            }
        })
        loadVideos()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            "add_video_local",
            viewLifecycleOwner
        ) { _, bundle ->
            val newVideo =
                bundle.getParcelable<Video>("new_video") ?: return@setFragmentResultListener
            videos.add(0, newVideo)
            adapter.notifyItemInserted(0)
            binding.recyclerView.scrollToPosition(0)
        }

        parentFragmentManager.setFragmentResultListener(
            "delete_video_local",
            viewLifecycleOwner
        ) { _, bundle ->
            val deletedVideoId =
                bundle.getString("deleted_video_id") ?: return@setFragmentResultListener
            val index = videos.indexOfFirst { it._id == deletedVideoId }
            if (index != -1) {
                videos.removeAt(index)
                adapter.notifyItemRemoved(index)
            }
        }

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("refresh_home")
            ?.observe(viewLifecycleOwner) { needRefresh ->
                if (needRefresh) {
                    page = 1
                    hasMore = true
                    isLoading = false
                    videos.clear()
                    adapter.notifyDataSetChanged()
                    loadVideos()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        refreshVideos()
    }

    private fun refreshVideos() {
        Log.d("HomeFragment", "refreshVideos() called")
        page = 1
        hasMore = true
        isLoading = false
        videos.clear()
        adapter.notifyDataSetChanged()
        loadVideos()
    }

    private fun loadVideos() {
        if (isLoading || !hasMore) return
        isLoading = true

        val timestamp = System.currentTimeMillis()
        Log.d("HomeFragment", "Loading videos - page: $page, timestamp: $timestamp")

        RetrofitClient.getInstance().videoApi.listVideos(
            page = page,
            limit = 10,
            timestamp = timestamp
        )
            .enqueue(object : Callback<VideoListResponse> {
                override fun onResponse(
                    call: Call<VideoListResponse>,
                    response: Response<VideoListResponse>
                ) {
                    Log.d("HomeFragment", "Video request URL: ${call.request().url}")
                    Log.d("HomeFragment", "Response code: ${response.code()}")
                    Log.d("HomeFragment", "Response body: ${response.body()}")

                    if (response.isSuccessful) {
                        val newVideos = response.body()?.videos ?: emptyList()
                        Log.d("HomeFragment", "Received ${newVideos.size} videos")
                        newVideos.forEachIndexed { index, video ->
                            Log.d(
                                "HomeFragment",
                                "Video $index: id=${video._id}, title=${video.title}, author=${video.author.username}"
                            )
                        }

                        if (page == 1) {
                            videos.clear()
                            Log.d("HomeFragment", "Cleared existing videos")
                        }
                        videos.addAll(newVideos)
                        Log.d("HomeFragment", "Total videos now: ${videos.size}")

                        adapter.notifyDataSetChanged()
                        hasMore = newVideos.size == 10
                        Log.d("HomeFragment", "Has more videos: $hasMore")
                        
                        if (hasMore) {
                            page++
                        }
                    } else {
                        Log.e(
                            "HomeFragment",
                            "Failed to load videos: ${response.errorBody()?.string()}"
                        )
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<VideoListResponse>, t: Throwable) {
                    Log.e("HomeFragment", "Network error loading videos: ${t.message}", t)
                    isLoading = false
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
