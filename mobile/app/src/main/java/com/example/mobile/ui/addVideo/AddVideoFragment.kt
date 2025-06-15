package com.example.mobile.ui.addVideo

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mobile.R
import com.example.mobile.api.RetrofitClient
import com.example.mobile.databinding.FragmentAddVideoBinding
import com.example.mobile.dto.user.User
import com.example.mobile.dto.video.Video
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream

class AddVideoFragment : Fragment() {

    private var _binding: FragmentAddVideoBinding? = null
    private val binding get() = _binding!!
    private var selectedPosterUri: Uri? = null
    private var selectedVideoUri: Uri? = null

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedPosterUri = it
                binding.posterPreview.setImageURI(it)
                binding.posterPreview.visibility = View.VISIBLE
            }
        }

    private val videoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedVideoUri = it
                binding.videoFileNameText.text = getFileName(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddVideoBinding.inflate(inflater, container, false)
        binding.selectPosterButton.setOnClickListener { imagePickerLauncher.launch("image/*") }
        binding.selectVideoButton.setOnClickListener { videoPickerLauncher.launch("video/*") }
        binding.addVideoButton.setOnClickListener { uploadVideo() }
        return binding.root
    }

    private fun getFileName(uri: Uri): String {
        if (uri.scheme == "content") {
            val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && it.moveToFirst()) {
                    return it.getString(nameIndex)
                }
            }
        }
        uri.path?.let { path ->
            val lastSlash = path.lastIndexOf('/')
            if (lastSlash != -1 && lastSlash < path.length - 1) {
                return path.substring(lastSlash + 1)
            }
        }
        return "unknown_file"
    }

    private fun prepareFilePart(name: String, uri: Uri): MultipartBody.Part {
        val contentResolver = requireContext().contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val fileBytes = inputStream?.readBytes() ?: byteArrayOf()
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        val requestBody = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val fileName = getFileName(uri)
        return MultipartBody.Part.createFormData(name, fileName, requestBody)
    }

    private fun uploadVideo() {
        val title = binding.videoNameEditText.text.toString()
        if (title.isEmpty() || selectedPosterUri == null || selectedVideoUri == null) {
            Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        val sharedPref = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", null) ?: return
        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val videoPart = prepareFilePart("video", selectedVideoUri!!)
        val posterPart = prepareFilePart("poster", selectedPosterUri!!)
        RetrofitClient.getInstance().videoApi.uploadVideo(token, titlePart, videoPart, posterPart)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Video uploaded!", Toast.LENGTH_SHORT)
                            .show()

                        val newVideo = Video(
                            _id = "temporary_id_${System.currentTimeMillis()}",
                            title = binding.videoNameEditText.text.toString(),
                            videoUrl = "",
                            posterUrl = "",
                            author = User(
                                _id = "current_user_id",
                                username = "current_username",
                                email = "",
                                password = "",
                                role = "",
                                avatarUrl = "current_user_avatar_url",
                                subscriptions = emptyList(),
                                followers = emptyList()
                            ),
                            createdAt = java.time.Instant.now().toString(),
                            likes = emptyList(),
                            dislikes = emptyList()
                        )

                        val bundle = Bundle().apply {
                            putParcelable("new_video", newVideo)
                        }
                        parentFragmentManager.setFragmentResult("add_video_local", bundle)

                        findNavController().popBackStack(R.id.navigation_home, false)
                    } else {
                        Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
