package com.translate.transcribelib

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File


class CustomCameraView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
   private var imageCapture: ImageCapture? = null
    private var camera : Camera? =null
   private lateinit var outputFileOptions: OutputFileOptions
   var isFlashOn  : Boolean = false
   private var cameraProvider: ProcessCameraProvider? = null
    fun startCamera(surfaceProvider: SurfaceProvider, lifecycle: LifecycleOwner) {
        val preview = Preview.Builder().build()
        val cameraProviderFuture = context?.let { ProcessCameraProvider.getInstance(it) }
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        val imageAnalysis = ImageAnalysis.Builder().setBackpressureStrategy(
            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
        ).build()
        val imageFile =
            File(context?.cacheDir?.absolutePath + System.currentTimeMillis().toString())

        outputFileOptions = ImageCapture.OutputFileOptions
            .Builder(imageFile)
            .build()
        preview.setSurfaceProvider(surfaceProvider)
        imageCapture = ImageCapture.Builder().build()
        context?.let { ContextCompat.getMainExecutor(it) }?.let {
           cameraProviderFuture?.addListener({
                cameraProvider = cameraProviderFuture.get()
            camera =  cameraProvider?.bindToLifecycle(
                    lifecycle,
                    cameraSelector,
                    imageAnalysis,
                    preview,
                    imageCapture
                )
            }, it)
        }
    }
    fun captureImage(listener: OnImageCaptureListener) {
        if ((imageCapture != null)) {
            imageCapture!!.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        listener.CapturedImage(outputFileResults.savedUri)
                        cameraProvider?.unbindAll()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        if(exception.toString().contains("Not bound to a valid Camera ")){
                            listener.onError()
                        }
                        else{
                            listener.onError()

                        }
                        Log.d("imageException", exception.toString())
                    }

                })
        } else {
            listener.onError()
        }

    }
    fun stopCamera(listener: OnCameraResponse) {
        if (imageCapture != null) {
            cameraProvider?.unbindAll()
            listener.response()
        } else {
            listener.response()
        }
    }
    fun flashOn(listener: OnCameraResponse){
        if(imageCapture !=null){
            if(camera?.cameraInfo?.hasFlashUnit()!!){
                camera?.cameraControl?.enableTorch(true)
                isFlashOn = true
            }
            else{
                listener.response()
            }

        }
        else{
            listener.response()
        }
    }
    fun flashOff(listener: OnCameraResponse){
        if(camera !=null){
            if(camera?.cameraInfo?.hasFlashUnit()!!){
                camera?.cameraControl?.enableTorch(false)
                isFlashOn = false
            }
            else{
                listener.response()
            }

        }
        else{
            listener.response()
        }
    }

}