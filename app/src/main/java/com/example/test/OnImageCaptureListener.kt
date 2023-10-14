@file:Suppress("EmptyMethod")

package com.translate.transcribelib

import android.net.Uri

interface OnImageCaptureListener {
    fun CapturedImage(uri: Uri?)
    fun onError()
}