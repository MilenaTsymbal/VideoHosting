package com.example.mobile.ui.video


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.content.res.Configuration
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.mobile.api.RetrofitClient
import com.example.mobile.databinding.FragmentVideoBinding
import com.example.mobile.dto.video.Comment
import com.example.mobile.dto.video.CommentRequest
import com.example.mobile.dto.video.LikeDislikeResponse
import com.example.mobile.dto.video.Video
import com.example.mobile.R
import com.example.mobile.util.CacheSingleton
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.mobile.dto.video.CommentsResponse
import es.dmoral.toasty.Toasty

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
        val prefs = requireContext()
            .getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        currentUserId = prefs.getString("_id", null)
        currentUserRole = prefs.getString("role", null)

        videoId = arguments?.getString("videoId")
            ?: throw IllegalArgumentException("videoId is required")
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        commentAdapter = CommentAdapter(comments)
        binding.commentsRecyclerView.adapter = commentAdapter
        loadVideo()
        binding.sendCommentButton.setOnClickListener { sendComment() }
        loadComments()
        return binding.root
    }

    private fun loadVideo() {
        RetrofitClient.getInstance().videoApi.getVideoById(videoId)
            .enqueue(object : Callback<Video> {
                override fun onResponse(call: Call<Video>, response: Response<Video>) {
                    if (response.isSuccessful && response.body() != null) {
                        video = response.body()!!
                        bindVideo(video!!)
                    }
                }

                override fun onFailure(call: Call<Video>, t: Throwable) {}
            })
    }

    private fun bindVideo(video: Video) {
        player = ExoPlayer.Builder(requireContext())
            .setMediaSourceFactory(DefaultMediaSourceFactory(getCacheDataSourceFactory()))
            .build()
        binding.playerView.player = player

        Glide.with(this).asBitmap().load("http://10.0.2.2:5000/${video.posterUrl}")
            .into(object : CustomTarget<android.graphics.Bitmap>() {
                override fun onResourceReady(
                    resource: android.graphics.Bitmap,
                    transition: Transition<in android.graphics.Bitmap>?
                ) {
                    binding.playerView.defaultArtwork = BitmapDrawable(resources, resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })

        val mediaItem = MediaItem.fromUri("http://10.0.2.2:5000/${video.videoUrl}")
        player?.setMediaItem(mediaItem)
        player?.playWhenReady = false
        player?.prepare()

        binding.videoTitle.text = video.title
        binding.authorName.text = video.author.username
        binding.publishDate.text = video.createdAt.substring(0, 10)
        Glide.with(this).load(video.author.avatarUrl).placeholder(R.drawable.ic_avatar)
            .into(binding.authorAvatar)

        likeCount = video.likes.size
        dislikeCount = video.dislikes.size
        liked = currentUserId != null && video.likes.contains(currentUserId)
        disliked = currentUserId != null && video.dislikes.contains(currentUserId)
        setupLikeDislikeButtons(video)
        setupDeleteButton(video)

        binding.shareIcon.setOnClickListener {
            ShareVideoDialog(requireContext(), videoId) {
        Toast.makeText(requireContext(), "Video shared!", Toast.LENGTH_SHORT).show()
    }.show()
        }

        loadComments()
        binding.sendCommentButton.setOnClickListener { sendComment() }
        binding.playerView.findViewById<ImageButton>(R.id.exo_fullscreen_button)
            ?.setOnClickListener { toggleFullscreen() }
    }

    private fun setupDeleteButton(video: Video) {
        val isOwner = video.author._id == currentUserId
        val isAdmin = currentUserRole == "admin"

        binding.deleteIcon.isVisible = isOwner || isAdmin

        binding.deleteIcon.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete video")
                .setMessage("Are you sure you want to delete this video?")
                .setPositiveButton("Delete") { _, _ ->
                    confirmDeleteVideo(video._id)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun confirmDeleteVideo(videoId: String) {
        val token = requireContext().getSharedPreferences("auth_prefs", 0).getString("token", null)
        if (token == null) {
            Toast.makeText(requireContext(), "Login to delete", Toast.LENGTH_SHORT).show()
            return
        }
        RetrofitClient.getInstance().videoApi.deleteVideo(videoId, token)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Video deleted", Toast.LENGTH_SHORT).show()

                        val bundle = Bundle().apply {
                            putString("deleted_video_id", videoId)
                        }
                        parentFragmentManager.setFragmentResult(
                            "delete_video_local",
                            Bundle().apply { putString("deleted_video_id", videoId) }
                        )
                        findNavController().popBackStack(R.id.navigation_home, false)
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun loadComments() {
        RetrofitClient.getInstance().videoApi
            .getComments(videoId)
            .enqueue(object : Callback<CommentsResponse> {
                override fun onResponse(
                    call: Call<CommentsResponse>,
                    response: Response<CommentsResponse>
                ) {
                    if (response.isSuccessful) {
                        comments.clear()
                        response.body()?.comments?.let { comments.addAll(it) }
                        commentAdapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<CommentsResponse>, t: Throwable) {
                    Toasty.error(
                        requireContext(),
                        "Failed to load comments: ${t.message}",
                        Toast.LENGTH_SHORT,
                        true
                    ).show()
                }
            })
    }

    private fun sendComment() {
        val commentText = binding.commentEditText.text.toString().trim()
        if (commentText.isEmpty()) return

        val token = requireContext()
            .getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("token", null) ?: return

        val request = CommentRequest(text = commentText)
        
        RetrofitClient.getInstance().videoApi
            .addComment(videoId, request, "$token") // Добавляем Bearer префикс
            .enqueue(object : Callback<Comment> {
                override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
                    if (response.isSuccessful) {
                        response.body()?.let { newComment ->
                            comments.add(0, newComment)
                            commentAdapter.notifyItemInserted(0)
                            binding.commentEditText.text.clear()
                            binding.commentsRecyclerView.scrollToPosition(0)
                        }
                    } else {
                        Toasty.error(requireContext(), 
                            "Failed to add comment: ${response.code()}", 
                            Toast.LENGTH_SHORT, true).show()
                    }
                }

                override fun onFailure(call: Call<Comment>, t: Throwable) {
                    Toasty.error(requireContext(), 
                        "Network error: ${t.message}", 
                        Toast.LENGTH_SHORT, true).show()
                }
            })
    }

    private fun getCacheDataSourceFactory(): CacheDataSource.Factory {
        val simpleCache = CacheSingleton.getInstance(requireContext())
        val upstreamFactory = DefaultDataSource.Factory(requireContext())
        return CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        val activity = requireActivity()
        val playerView = binding.playerView
        if (isFullscreen) {
            originalPlayerViewParams = playerView.layoutParams
            originalParent = playerView.parent as ViewGroup
            originalIndex = originalParent!!.indexOfChild(playerView)
            originalParent!!.removeView(playerView)
            val decor = activity.window.decorView as ViewGroup
            val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            playerView.layoutParams = params
            decor.addView(playerView)
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            hideOtherUIElements(true)
            (activity as AppCompatActivity).supportActionBar?.hide()
        } else {
            val decor = activity.window.decorView as ViewGroup
            decor.removeView(playerView)
            originalParent?.addView(playerView, originalIndex)
            playerView.layoutParams = originalPlayerViewParams
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            hideOtherUIElements(false)
            (activity as AppCompatActivity).supportActionBar?.show()
        }
    }

    private fun hideOtherUIElements(hide: Boolean) {
        binding.videoTitle.isVisible = !hide
        binding.authorAvatar.isVisible = !hide
        binding.authorName.isVisible = !hide
        binding.publishDate.isVisible = !hide
        binding.actionButtons.isVisible = !hide
        binding.commentEditText.isVisible = !hide
        binding.sendCommentButton.isVisible = !hide
        binding.commentsRecyclerView.isVisible = !hide
    }

    private fun handleLike(videoId: String) {
        val token =
            requireContext().getSharedPreferences("auth_prefs", 0).getString("token", null) ?: run {
                Toast.makeText(requireContext(), "Login to like", Toast.LENGTH_SHORT).show()
                return
            }
        RetrofitClient.getInstance().videoApi.likeVideo(videoId, token)
            .enqueue(object : Callback<LikeDislikeResponse> {
                override fun onResponse(
                    call: Call<LikeDislikeResponse>,
                    response: Response<LikeDislikeResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!
                        likeCount = result.likes
                        dislikeCount = result.dislikes
                        if (liked) liked = false else {
                            liked = true; disliked = false
                        }
                        updateLikeDislikeUI()
                    } else Toast.makeText(
                        requireContext(),
                        "Failed to like video",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(call: Call<LikeDislikeResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun handleDislike(videoId: String) {
        val token =
            requireContext().getSharedPreferences("auth_prefs", 0).getString("token", null) ?: run {
                Toast.makeText(requireContext(), "Login to dislike", Toast.LENGTH_SHORT).show()
                return
            }
        RetrofitClient.getInstance().videoApi.dislikeVideo(videoId, token)
            .enqueue(object : Callback<LikeDislikeResponse> {
                override fun onResponse(
                    call: Call<LikeDislikeResponse>,
                    response: Response<LikeDislikeResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!
                        likeCount = result.likes
                        dislikeCount = result.dislikes
                        if (disliked) disliked = false else {
                            disliked = true; liked = false
                        }
                        updateLikeDislikeUI()
                    } else Toast.makeText(
                        requireContext(),
                        "Failed to dislike video",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(call: Call<LikeDislikeResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun setupLikeDislikeButtons(video: Video) {
        binding.likeCountText.text = likeCount.toString()
        binding.dislikeCountText.text = dislikeCount.toString()
        updateLikeDislikeUI()
        binding.likeIcon.setOnClickListener { handleLike(video._id) }
        binding.dislikeIcon.setOnClickListener { handleDislike(video._id) }
    }

    private fun updateLikeDislikeUI() {
        binding.likeIcon.setImageResource(if (liked) R.drawable.ic_like_active else R.drawable.ic_like)
        binding.dislikeIcon.setImageResource(if (disliked) R.drawable.ic_dislike_active else R.drawable.ic_dislike)
        binding.likeCountText.text = likeCount.toString()
        binding.dislikeCountText.text = dislikeCount.toString()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack(R.id.navigation_home, false)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        player?.let {
            playbackPosition = it.currentPosition
            playWhenReady = it.playWhenReady
            it.playWhenReady = false
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
        _binding = null
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val activity = requireActivity() as AppCompatActivity
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            activity.supportActionBar?.hide()
            requireActivity().findViewById<android.view.View>(R.id.nav_view)?.visibility = View.GONE
            requireActivity().window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        } else {
            activity.supportActionBar?.show()
            requireActivity().findViewById<android.view.View>(R.id.nav_view)?.visibility =
                View.VISIBLE
            requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
}
