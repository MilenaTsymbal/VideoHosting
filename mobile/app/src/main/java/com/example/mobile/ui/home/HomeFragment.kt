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
import com.example.mobile.api.RetrofitClient
import com.example.mobile.databinding.FragmentHomeBinding
import com.example.mobile.dto.video.Video
import com.example.mobile.dto.video.VideoListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import com.example.mobile.R
import com.example.mobile.ui.home.VideoAdapter

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
                if (!isLoading && hasMore && last >= total - 3) loadVideos()
            }
        })
        loadVideos()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обработка добавления видео
        parentFragmentManager.setFragmentResultListener("add_video_local", viewLifecycleOwner) { _, bundle ->
            val newVideo = bundle.getParcelable<Video>("new_video") ?: return@setFragmentResultListener
            videos.add(0, newVideo) // Добавляем новое видео в начало списка
            adapter.notifyItemInserted(0) // Уведомляем адаптер
            binding.recyclerView.scrollToPosition(0) // Прокручиваем к началу списка
        }

        // Обработка удаления видео
        parentFragmentManager.setFragmentResultListener("delete_video_local", viewLifecycleOwner) { _, bundle ->
            val deletedVideoId = bundle.getString("deleted_video_id") ?: return@setFragmentResultListener
            val index = videos.indexOfFirst { it._id == deletedVideoId }
            if (index != -1) {
                videos.removeAt(index) // Удаляем видео из списка
                adapter.notifyItemRemoved(index) // Уведомляем адаптер
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

        val timestamp = System.currentTimeMillis() // Уникальный параметр для игнорирования кэша
        RetrofitClient.getInstance().videoApi.listVideos(page = page, limit = 10, timestamp = timestamp)
            .enqueue(object : Callback<VideoListResponse> {
                override fun onResponse(call: Call<VideoListResponse>, response: Response<VideoListResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val newVideos = response.body()!!.videos

                        if (page == 1) {
                            // Очищаем список только при загрузке первой страницы
                            videos.clear()
                        }

                        // Добавляем только уникальные видео
                        val existingIds = videos.map { it._id }.toSet()
                        val uniqueVideos = newVideos.filter { it._id !in existingIds }
                        videos.addAll(uniqueVideos)

                        // Уведомляем адаптер об изменениях
                        adapter.notifyDataSetChanged()

                        // Проверяем, есть ли ещё данные для загрузки
                        hasMore = newVideos.isNotEmpty()
                        if (hasMore) page++
                    } else {
                        Log.e("HomeFragment", "Failed to fetch videos: ${response.errorBody()?.string()}")
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<VideoListResponse>, t: Throwable) {
                    Log.e("HomeFragment", "Video list load error: ${t.message}")
                    isLoading = false
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
