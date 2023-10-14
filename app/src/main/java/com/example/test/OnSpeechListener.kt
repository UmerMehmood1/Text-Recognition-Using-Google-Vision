package com.translate.transcribelib

import java.lang.Error

interface OnSpeechListener {
    fun OnError(error: String)
    fun onComplete()
}