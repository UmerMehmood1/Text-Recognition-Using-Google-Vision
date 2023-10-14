package com.translate.transcribelib

interface OnTranslationListener {
    fun OnTranslate(resultModel: ResultModel)
    fun OnTranslateError(error: String)
}