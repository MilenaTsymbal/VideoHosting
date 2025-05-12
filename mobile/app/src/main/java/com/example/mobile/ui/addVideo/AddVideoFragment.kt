package com.example.mobile.ui.addVideo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.mobile.databinding.FragmentAddVideoBinding
import android.widget.Toast
import android.provider.OpenableColumns
import androidx.core.net.toFile

class AddVideoFragment : Fragment() {

    private lateinit var binding: FragmentAddVideoBinding

    private var selectedPosterUri: Uri? = null
    private var selectedVideoUri: Uri? = null

    // Launcher for image selection
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedPosterUri = it
                binding.posterPreview.setImageURI(it)
                binding.posterPreview.visibility = View.VISIBLE
            }
        }

    // Launcher for video selection
    private val videoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedVideoUri = it
                binding.videoFileNameText.text = getFileName(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddVideoBinding.inflate(inflater, container, false)

        // Button listeners
        binding.selectPosterButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.selectVideoButton.setOnClickListener {
            videoPickerLauncher.launch("video/*")
        }

        return binding.root
    }

    private fun getFileName(uri: Uri): String {
        if (uri.scheme == "content") {
            val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && it.moveToFirst()) {
                    val fileName = it.getString(nameIndex)
                    if (fileName != null) return fileName
                }
            }
        }

        // fallback: extract from path
        uri.path?.let { path ->
            val lastSlash = path.lastIndexOf('/')
            if (lastSlash != -1 && lastSlash < path.length - 1) {
                return path.substring(lastSlash + 1)
            }
        }

        return "unknown_file"
    }

}
