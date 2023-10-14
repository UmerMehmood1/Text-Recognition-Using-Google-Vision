package com.translate.transcribelib

class LanguageModel(
    val lang_name: String,
    val lang_flag: String,
    val lang_code: String,
    val voice_code: String,
    val is_voice: String,
    val is_speech: String,
    val is_ocr_supported: String,
    val ocr_lang_script: String
) {
}