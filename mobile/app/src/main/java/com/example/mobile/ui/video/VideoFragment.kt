package com.example.mobile.ui.video

import android.app.AlertDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mobile.R
import com.example.mobile.api.RetrofitClient
import com.example.mobile.databinding.FragmentVideoBinding
import com.example.mobile.dto.video.Comment
import com.example.mobile.dto.video.CommentRequest
import com.example.mobile.dto.video.CommentsResponse
import com.example.mobile.dto.video.LikeDislikeResponse
import com.example.mobile.dto.video.Video
import com.example.mobile.util.CacheSingleton
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class VideoFragment : Fragment() {
    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!
    private lateinit var videoId: String
    private var currentUserId: String? = null
    private var currentUserRole: String? = null
    private var video: Video? = null
    private val comments = mutableListOf<Comment>()
    private lateinit var commentAdapter: CommentAdapter
    private var player: ExoPlayer? = null
    private var playbackPosition: Long = 0
    private var playWhenReady: Boolean = false
    private var isFullscreen = false
    private var originalPlayerViewParams: ViewGroup.LayoutParams? = null
    private var originalParent: ViewGroup? = null
    private var originalIndex: Int = -1
    private var likeCount = 0
    private var dislikeCount = 0
    private var liked = false
    private var disliked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        currentUserId = prefs.getString("_id", null)
        currentUserRole = prefs.getString("role", null)

        videoId = arguments?.getString("videoId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)

        setupRecyclerView()
        setupClickListeners()
        loadVideo()
        loadComments()
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter(comments)
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.commentsRecyclerView.adapter = commentAdapter
    }

    private fun setupClickListeners() {
        binding.sendCommentButton.setOnClickListener {
            sendComment()
        }

        binding.likeIcon.setOnClickListener {
            handleLike(videoId)
        }

        binding.dislikeIcon.setOnClickListener {
            handleDislike(videoId)
        }

        binding.shareIcon.setOnClickListener {
            ShareVideoDialog(requireContext(), videoId) {
                Toast.makeText(requireContext(), "Video shared!", Toast.LENGTH_SHORT).show()
            }.show()
        }
    }

    private fun loadVideo() {
        val videoApi = RetrofitClient.getInstance().videoApi
        videoApi.getVideoById(videoId).enqueue(object : Callback<Video> {
            override fun onResponse(call: Call<Video>, response: Response<Video>) {
                if (response.isSuccessful && response.body() != null) {
                    video = response.body()!!
                    bindVideo(video!!)
                } else {
                    Toast.makeText(requireContext(), "Failed to load video", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<Video>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun bindVideo(video: Video) {
        binding.videoTitle.text = video.title
        binding.authorName.text = video.author.username

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(video.createdAt)
            binding.publishDate.text = date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            binding.publishDate.text = ""
        }

        Glide.with(this)
            .load(video.author.avatarUrl)
            .placeholder(R.drawable.ic_avatar)
            .into(binding.authorAvatar)

        setupVideoPlayer(video)

        setupLikeDislikeButtons(video)

        setupDeleteButton(video)

        binding.authorAvatar.setOnClickListener { navigateToChannel(video.author.username) }
        binding.authorName.setOnClickListener { navigateToChannel(video.author.username) }
    }

    private fun setupVideoPlayer(video: Video) {
        player = ExoPlayer.Builder(requireContext())
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(getCacheDataSourceFactory())
            )
            .build()

        binding.playerView.player = player

        val videoUrl = "http://10.0.2.2:5000/${video.videoUrl}"
        val mediaItem = MediaItem.fromUri(videoUrl)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = playWhenReady
        player?.seekTo(playbackPosition)

        val fullscreenButton =
            binding.playerView.findViewById<ImageButton>(R.id.exo_fullscreen_button)
        fullscreenButton?.setOnClickListener { toggleFullscreen() }
    }

    private fun navigateToChannel(username: String) {
        val prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val currentUsername = prefs.getString("username", null)

        val bundle = Bundle().apply {
            putString("username", username)
            putBoolean("isOwnProfile", username == currentUsername)
        }
        findNavController().navigate(R.id.navigation_channel, bundle)
    }

    private fun setupDeleteButton(video: Video) {
        val canDelete = (currentUserId == video.author._id) || (currentUserRole == "admin")

        if (canDelete) {
            binding.deleteIcon.visibility = View.VISIBLE
            binding.deleteIcon.setOnClickListener {
                confirmDeleteVideo(videoId)
            }
        } else {
            binding.deleteIcon.visibility = View.GONE
        }
    }

    private fun confirmDeleteVideo(videoId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Video")
            .setMessage("Are you sure you want to delete this video?")
            .setPositiveButton("Delete") { _, _ ->
                deleteVideo(videoId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteVideo(videoId: String) {
        val token = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("token", null) ?: return

        val videoApi = RetrofitClient.getInstance().videoApi
        videoApi.deleteVideo(videoId, token).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Video deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    val bundle = Bundle().apply {
                        putString("deleted_video_id", videoId)
                    }
                    parentFragmentManager.setFragmentResult("delete_video_local", bundle)

                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete video", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadComments() {
        val videoApi = RetrofitClient.getInstance().videoApi
        videoApi.getComments(videoId).enqueue(object : Callback<CommentsResponse> {
            override fun onResponse(
                call: Call<CommentsResponse>,
                response: Response<CommentsResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    comments.clear()
                    comments.addAll(response.body()!!.comments)
                    commentAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<CommentsResponse>, t: Throwable) {
                Log.e("VideoFragment", "Failed to load comments: ${t.message}")
            }
        })
    }

    private fun sendComment() {
        val commentText = binding.commentEditText.text.toString().trim()
        if (commentText.isEmpty()) return

        val token = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("token", null) ?: return

        val commentRequest = CommentRequest(commentText)
        val videoApi = RetrofitClient.getInstance().videoApi

        videoApi.addComment(videoId, commentRequest, token).enqueue(object : Callback<Comment> {
            override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
                if (response.isSuccessful && response.body() != null) {
                    comments.add(0, response.body()!!)
                    commentAdapter.notifyItemInserted(0)
                    binding.commentEditText.text.clear()
                    binding.commentsRecyclerView.scrollToPosition(0)
                } else {
                    Toast.makeText(requireContext(), "Failed to add comment", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<Comment>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getCacheDataSourceFactory(): CacheDataSource.Factory {
        val cache = CacheSingleton.getInstance(requireContext())
        val defaultDataSourceFactory = DefaultDataSource.Factory(requireContext())

        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(defaultDataSourceFactory)
    }

    private fun toggleFullscreen() {
        if (isFullscreen) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

            originalParent?.let { parent ->
                (binding.playerView.parent as? ViewGroup)?.removeView(binding.playerView)
                if (originalIndex != -1) {
                    parent.addView(binding.playerView, originalIndex)
                } else {
                    parent.addView(binding.playerView)
                }
                binding.playerView.layoutParams = originalPlayerViewParams
            }

            hideOtherUIElements(false)
            isFullscreen = false
        } else {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            originalParent = binding.playerView.parent as? ViewGroup
            originalPlayerViewParams = binding.playerView.layoutParams
            originalIndex = originalParent?.indexOfChild(binding.playerView) ?: -1

            originalParent?.removeView(binding.playerView)
            val decorView = requireActivity().window.decorView as ViewGroup
            decorView.addView(
                binding.playerView, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            hideOtherUIElements(true)
            isFullscreen = true
        }
    }

    private fun hideOtherUIElements(hide: Boolean) {
        val visibility = if (hide) View.GONE else View.VISIBLE

        val flags = if (hide) {
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            View.SYSTEM_UI_FLAG_VISIBLE
        }

        requireActivity().window.decorView.systemUiVisibility = flags

        val actionBar = (requireActivity() as? AppCompatActivity)?.supportActionBar
        if (hide) {
            actionBar?.hide()
        } else {
            actionBar?.show()
        }
    }

    private fun setupLikeDislikeButtons(video: Video) {
        currentUserId?.let { userId ->
            liked = video.likes.contains(userId)
            disliked = video.dislikes.contains(userId)
            likeCount = video.likes.size
            dislikeCount = video.dislikes.size

            updateLikeDislikeUI()

            binding.likeIcon.setOnClickListener {
                handleLike(video._id)
            }

            binding.dislikeIcon.setOnClickListener {
                handleDislike(video._id)
            }
        } ?: run {
            likeCount = video.likes.size
            dislikeCount = video.dislikes.size
            binding.likeCountText.text = likeCount.toString()
            binding.dislikeCountText.text = dislikeCount.toString()

            binding.likeIcon.setImageResource(R.drawable.ic_like)
            binding.dislikeIcon.setImageResource(R.drawable.ic_dislike)

            binding.likeIcon.setOnClickListener(null)
            binding.dislikeIcon.setOnClickListener(null)
        }
    }

    private fun updateLikeDislikeUI() {
        binding.likeIcon.setImageResource(
            if (liked) R.drawable.ic_like_active else R.drawable.ic_like
        )

        binding.dislikeIcon.setImageResource(
            if (disliked) R.drawable.ic_dislike_active else R.drawable.ic_dislike
        )

        binding.likeCountText.text = likeCount.toString()
        binding.dislikeCountText.text = dislikeCount.toString()
    }

    private fun handleLike(videoId: String) {
        currentUserId?.let { userId ->
            val token = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                .getString("token", null) ?: return

            RetrofitClient.getInstance().videoApi.likeVideo(videoId, token)
                .enqueue(object : Callback<LikeDislikeResponse> {
                    override fun onResponse(
                        call: Call<LikeDislikeResponse>,
                        response: Response<LikeDislikeResponse>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let { result ->
                                likeCount = result.likes
                                dislikeCount = result.dislikes

                                liked = !liked
                                if (liked && disliked) {
                                    disliked = false
                                }

                                updateLikeDislikeUI()
                            }
                        } else {
                            Toasty.error(
                                requireContext(),
                                "Failed to like video",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<LikeDislikeResponse>, t: Throwable) {
                        Toasty.error(
                            requireContext(),
                            "Network error: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        } ?: run {
            Toasty.info(requireContext(), "Please login to like videos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleDislike(videoId: String) {
        currentUserId?.let { userId ->
            val token = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                .getString("token", null) ?: return

            RetrofitClient.getInstance().videoApi.dislikeVideo(videoId, token)
                .enqueue(object : Callback<LikeDislikeResponse> {
                    override fun onResponse(
                        call: Call<LikeDislikeResponse>,
                        response: Response<LikeDislikeResponse>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let { result ->
                                likeCount = result.likes
                                dislikeCount = result.dislikes

                                disliked = !disliked
                                if (disliked && liked) {
                                    liked = false
                                }

                                updateLikeDislikeUI()
                            }
                        } else {
                            Toasty.error(
                                requireContext(),
                                "Failed to dislike video",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<LikeDislikeResponse>, t: Throwable) {
                        Toasty.error(
                            requireContext(),
                            "Network error: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        } ?: run {
            Toasty.info(requireContext(), "Please login to dislike videos", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onPause() {
        super.onPause()
        player?.let {
            playbackPosition = it.currentPosition
            playWhenReady = it.playWhenReady
            it.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        player?.let {
            it.seekTo(playbackPosition)
            it.playWhenReady = playWhenReady
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        player = null
        _binding = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideOtherUIElements(true)
        } else {
            hideOtherUIElements(false)
        }
    }
}
