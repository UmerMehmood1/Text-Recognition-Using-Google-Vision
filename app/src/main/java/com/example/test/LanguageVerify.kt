package com.translate.transcribelib

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream


class LanguageVerify(val context: Context) {
    fun verifyLangByCode(code: String): LanguageModel {
        val jsonArray = readJsonFile(context = context)
        for (item in 0 until jsonArray.length()) {
            val jsonData = jsonArray[item] as JSONObject
            val lan_code = jsonData.getString("lang_code")
            val voice_code = jsonData.getString("voice_code")
            if (lan_code.equals(code)||voice_code.equals(code)) {
                return LanguageModel(
                    jsonData.getString("lang_name").toString(),
                    jsonData.getString("lang_flag").toString(),
                    jsonData.getString("lang_code").toString(),
                    jsonData.getString("voice_code").toString(),
                    jsonData.getString("is_voice").toString(),
                    jsonData.getString("is_speech").toString(),
                    jsonData.getString("is_ocr_supported").toString(),
                    jsonData.getString("ocr_lang_script").toString(),
                )

            }
        }
        return LanguageModel("", "", "", "", "", "", "", "")
    }

    private fun readJsonFile(context: Context): JSONArray {
        val json: String
        try {
            val am = context.assets
            val inputStream: InputStream = am.open("data_ocr.json")
            json = inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            throw e
        }
        val jsonObject = JSONObject(json)
        return jsonObject.getJSONArray("all_languages")

    }
}