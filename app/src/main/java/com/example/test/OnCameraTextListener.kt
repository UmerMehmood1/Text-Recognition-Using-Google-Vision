package com.translate.transcribelib

import com.google.mlkit.vision.text.Text

interface OnCameraTextListener {
    fun onResult(result: Text)
    fun OnFailed()

}