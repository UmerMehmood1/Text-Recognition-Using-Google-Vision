package com.example.test

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.example.test.R
import com.example.test.databinding.FragmentCameraPictureBinding
import com.translate.transcribelib.OnCameraResponse
import com.translate.transcribelib.OnImageCaptureListener

class CameraPictureFragment(private var fragmentManager: FragmentManager) : Fragment() {
    private lateinit var binding: FragmentCameraPictureBinding
    var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraPictureBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ResourceAsColor")
    private fun loadFrag(fragment: Fragment) {
        if (fragment is CameraScanFragment){
            (activity as? CameraScreen)?.binding?.selectionLanguageOne?.setTextColor(androidx.appcompat.R.color.material_blue_grey_800)
        }
        if (requireActivity() != null) {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_view, fragment).commit()
        }
    }

    private fun setListeners() {
        binding.captureButton.setOnClickListener {
            binding.customCameraView.captureImage(object : OnImageCaptureListener {
                override fun CapturedImage(uri: Uri?) {
                    imageUri = uri
                    loadFrag(CameraScanFragment(imageUri, fragmentManager))
                }

                override fun onError() {
                }


            })

        }
        binding.flashButton.setOnClickListener {
            if (binding.customCameraView.isFlashOn) {
                binding.flashButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.baseline_flash_on_24
                    )
                )
                binding.customCameraView.flashOff(object : OnCameraResponse {
                    override fun response() {
                    }


                })
            } else {
                binding.flashButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.baseline_flash_off_24
                    )
                )
                binding.customCameraView.flashOn(object : OnCameraResponse {
                    override fun response() {
                    }


                })
            }
        }
        binding.galleryButton.setOnClickListener {
            pickImage()
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            imageUri = data?.data
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onResume() {
        super.onResume()
        if (imageUri != null) {
            loadFrag(CameraScanFragment(imageUri,fragmentManager))
        } else {
            binding.customCameraView.startCamera(
                binding.cameraPreviewview.surfaceProvider, this as LifecycleOwner
            )
            if (binding.customCameraView.isFlashOn) {
                binding.customCameraView.flashOff(object : OnCameraResponse {
                    override fun response() {
                    }

                })
            }
            setListeners()
        }
    }


    override fun onPause() {
        super.onPause()
        binding.customCameraView.stopCamera(object : OnCameraResponse {
            override fun response() {
            }

        })
    }


}
