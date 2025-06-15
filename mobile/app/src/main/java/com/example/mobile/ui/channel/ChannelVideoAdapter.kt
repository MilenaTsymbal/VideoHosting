package com.example.mobile.ui.channel

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobile.R
import com.example.mobile.databinding.ItemChannelVideoBinding
import com.example.mobile.dto.video.Video
import java.text.SimpleDateFormat
import java.util.*

class ChannelVideoAdapter(
    private val videos: MutableList<Video>,
    private val onClick: (Video) -> Unit
) : RecyclerView.Adapter<ChannelVideoAdapter.VideoViewHolder>() {

    fun updateVideos(newVideos: List<Video>) {
        Log.d("ChannelVideoAdapter", "updateVideos called with ${newVideos.size} videos")
        videos.clear()
        videos.addAll(newVideos)
        notifyDataSetChanged()
        Log.d("ChannelVideoAdapter", "Adapter now has ${videos.size} videos")
        
        newVideos.forEachIndexed { index, video ->
            Log.d("ChannelVideoAdapter", "Video $index: ${video.title} - ${video.createdAt}")
        }
    }

    inner class VideoViewHolder(private val binding: ItemChannelVideoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(video: Video) {
            Log.d("ChannelVideoAdapter", "Binding video at position $adapterPosition: ${video.title}")
            binding.videoTitle.text = video.title
            
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(video.createdAt)
                binding.videoDate.text = date?.let { outputFormat.format(it) } ?: ""
            } catch (e: Exception) {
                binding.videoDate.text = ""
            }

            Glide.with(binding.root.context)
                .load("http://10.0.2.2:5000/${video.posterUrl}")
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(binding.videoPoster)

            binding.root.setOnClickListener { 
                Log.d("ChannelVideoAdapter", "Video clicked: ${video.title}")
                onClick(video) 
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemChannelVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        if (position < videos.size) {
            holder.bind(videos[position])
        } else {
            Log.e("ChannelVideoAdapter", "Position $position out of bounds, videos size: ${videos.size}")
        }
    }

    override fun getItemCount(): Int {
        Log.d("ChannelVideoAdapter", "getItemCount: ${videos.size}")
        return videos.size
    }
}