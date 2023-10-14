package com.translate.transcribelib

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class SharedPrefenecensHelper(context: Context) {
   private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)

    fun saveString(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }
    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun saveModel(key: String, value: ResultModel) {
        val gson =  Gson();
        val json = gson.toJson(value);
        val editor = sharedPreferences.edit()
        editor.putString(key, json.toString())
        editor.apply()
    }

    fun getModel(key: String): ResultModel {
        val gson = Gson()
        val json: String? = sharedPreferences.getString(key, "")
        val obj: ResultModel = gson.fromJson(json, ResultModel::class.java)
        return obj
    }
}