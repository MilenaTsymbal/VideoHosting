package com.example.mobile.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobile.R
import com.example.mobile.databinding.ItemVideoBinding
import com.example.mobile.dto.video.Video

class VideoAdapter(
    private val videos: List<Video>,
    private val onClick: (Video) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(val binding: ItemVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(video: Video) {
            binding.videoTitle.text = video.title
            binding.authorName.text = video.author.username

            Glide.with(binding.root.context)
                .load("http://10.0.2.2:5000/${video.posterUrl}")
                .into(binding.videoPoster)
            Glide.with(binding.root.context)
                .load(video.author.avatarUrl)
                .placeholder(R.drawable.ic_avatar)
                .into(binding.authorAvatar)

            binding.root.setOnClickListener { onClick(video) }

            binding.authorAvatar.setOnClickListener {
                navigateToChannel(video.author.username)
            }

            binding.authorName.setOnClickListener {
                navigateToChannel(video.author.username)
            }
        }

        private fun navigateToChannel(username: String) {
            val prefs =
                binding.root.context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val currentUsername = prefs.getString("username", null)

            val bundle = Bundle().apply {
                putString("username", username)
                putBoolean("isOwnProfile", username == currentUsername)
            }
            binding.root.findNavController().navigate(R.id.navigation_channel, bundle)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun getItemCount() = videos.size

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videos[position])
    }
}