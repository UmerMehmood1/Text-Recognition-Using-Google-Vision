package com.example.test

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.test.databinding.FragmentCameraScanBinding
import com.google.mlkit.vision.text.Text
import com.translate.transcribelib.OnCameraTextListener
import com.translate.transcribelib.Transcribe
import java.io.File
import java.io.FileOutputStream


class CameraScanFragment(private var imageUri: Uri?, fragmentManager: FragmentManager) :
    Fragment() {
    private lateinit var binding: FragmentCameraScanBinding
    private var drawable: Drawable? = null
    private var scaledImageUri: Uri? = null
    private var screenWidth: Int? = 720
    private var screenHeight: Int? = 720
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraScanBinding.inflate(inflater, container, false)
        setListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var uri = imageUri
        var bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri);
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, screenWidth!!, screenHeight!!, true)
        scaledImageUri = Uri.parse(saveBitmapToUri(scaledBitmap))
        binding.scannedImageview.setImageURI(scaledImageUri)
    }

    private fun saveBitmapToUri(bitmap: Bitmap,): String? {
        try {
            val file = File(requireContext().filesDir, "scaled_image.jpg")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
            return Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun setListeners() {
        binding.scanNowButton.setOnClickListener {
            binding.scanNowButton.visibility = View.GONE
            binding.scanButtonProgressBar.visibility = View.VISIBLE

            binding.textSelection.layoutParams.width = screenWidth!!
            binding.textSelection.layoutParams.height = screenHeight!!
            binding.textSelection.setCustomWidth(screenWidth!!)
            binding.textSelection.setCustomHeight(screenHeight!!)

            binding.textoverlayviewgroup.layoutParams.width = screenWidth!!
            binding.textoverlayviewgroup.layoutParams.height = screenHeight!!
            binding.textoverlayviewgroup.setCustomWidth(screenWidth!!)
            binding.textoverlayviewgroup.setCustomHeight(screenHeight!!)


            val transcribe = Transcribe(requireContext())
            transcribe.getTextfromImage(scaledImageUri, object : OnCameraTextListener {
                override fun onResult(result: Text) {
                    binding.textSelection.setTextList(result)
                    binding.textoverlayviewgroup.setSelectionBackgroundColor(R.color.selectedTextBG)
                    binding.textoverlayviewgroup.addTextModel(result)
                    binding.scanButtonProgressBar.visibility = View.GONE
                    binding.scanNowButton.text = "Translate"
                    binding.scanNowButton.visibility = View.VISIBLE
                    binding.scanNowButton.setOnClickListener {
                        Toast.makeText(
                            requireContext(),
                            "Translating... Duh",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun OnFailed() {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        if (imageUri != null){
        }
    }

}