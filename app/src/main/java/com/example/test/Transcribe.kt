package com.translate.transcribelib

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.Executors


@SuppressLint("SuspiciousIndentation")
class Transcribe(private val context: Context) {
    private val mediaPlayer = MediaPlayer()
    private val isPlaying: Boolean = false
    private var mediaLength: Int = 0
    private var startIndex = 0
    private val executor = Executors.newSingleThreadExecutor()
    fun getTranslation(
        content: String,
        souLang: String,
        targetLang: String,
        listener: OnTranslationListener
    ) {
        if (isInternetConnected(context)) {
            val URL: String = "https://translate.googleapis.com/translate_a/single?client=gtx&sl="
            lateinit var queue: RequestQueue
            val CONNECTION_TIME_OUT = 500
            val TOO_MANY_REQUEST_CODE = 429
            val REQUEST_TIME_OUT = 408
            val REQUEST_NOT_FOUND = 404
            val endPoint: String
            var resultModel: ResultModel? = null
            queue = Volley.newRequestQueue(context)
            endPoint = "$souLang&tl=$targetLang&dt=t&q=${URLEncoder.encode(content, "UTF-8")}"
            val request_URL = URL + endPoint
            val languageModel = LanguageVerify(context = context).verifyLangByCode(targetLang)

            if (SharedPrefenecensHelper(context).getString("last_promt", "")
                    .toString() != request_URL
            ) {
                val stringRequest = object : StringRequest(Method.GET, request_URL, { response ->

                    val stringBuilder = StringBuilder()
                    val main = JSONArray(response.toString())
                    val total = main.get(0) as JSONArray
                    var sourceLang = ""
                    try {
                        sourceLang = main.get(2) as String
                    } catch (e: IndexOutOfBoundsException) {
                        e.printStackTrace()
                    }
                    for (i in 0 until total.length()) {
                        val currentLine = total[i] as JSONArray
                        stringBuilder.append(currentLine[0].toString())
                    }

                    resultModel = ResultModel(stringBuilder.toString(), languageModel)
                    SharedPrefenecensHelper(context).saveModel("last_model", resultModel!!)
                    listener.OnTranslate(resultModel!!)
                }, {
                    when {
                        it != null -> {
                            val networkResponse = it.networkResponse
                            if (networkResponse != null) {
                                Log.d("statusCode", networkResponse.statusCode.toString())

                                when (networkResponse.statusCode) {
                                    CONNECTION_TIME_OUT -> {
                                        listener.OnTranslateError("Connection time out")
                                    }

                                    TOO_MANY_REQUEST_CODE -> {
                                        listener.OnTranslateError("Too many requests")
                                    }

                                    REQUEST_TIME_OUT -> {
                                        listener.OnTranslateError("Request time out")
                                    }

                                    REQUEST_NOT_FOUND -> {
                                        listener.OnTranslateError("Request not found")
                                    }

                                    else -> listener.OnTranslateError("Something went wrong")
                                }
                            } else {
                                listener.OnTranslateError("Connection Failed")
                            }

                        }
                    }
                }

                ) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["User-Agent"] =
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36"
                        headers["content-type"] = "application/json; charset=utf-8"
                        headers["accept"] =
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
                        return headers
                    }
                }

                queue.add(stringRequest)
            } else {
                resultModel = SharedPrefenecensHelper(context).getModel("last_model")
                try {
                    listener.OnTranslate(resultModel!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }


            }
            SharedPrefenecensHelper(context).saveString("last_promt", request_URL)

            Log.d("endpoint", request_URL)
        } else {
            listener.OnTranslateError("No Internet Connection")
        }


    }

    fun getSpeech(content: String, voiceCode: String, listener: OnSpeechListener) {
        if (isInternetConnected(context)) {
            if (startIndex >= content.length) {
                return listener.onComplete()
            }
            var endIndex = startIndex + 154
            if (endIndex > content.length) {
                endIndex = content.length
            }
            val str = content.substring(startIndex, endIndex)
            startIndex = endIndex
            initspeech(mediaPlayer)
            Log.d("resp", content.toString())
            val URL: String = "https://translate.google.com/translate_tts?ie=UTF-8&tl="
            val languageModel = LanguageVerify(context = context).verifyLangByCode(voiceCode)
            val REQUEST_URL = "$URL$voiceCode&client=tw-ob&q=$str"
            if (languageModel.is_speech.equals("true")) {

                executor.execute(Runnable {
                    if (!mediaPlayer.isPlaying) {
                        mediaPlayer.setDataSource(REQUEST_URL)
                        mediaPlayer.prepare()
                        mediaPlayer.start()

//                    Log.d("speech_url", REQUEST_URL)
                    }
                })
                mediaPlayer.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
                    override fun onCompletion(mp: MediaPlayer?) {
                        listener.onComplete()
                        mediaPlayer.reset()
                        getSpeech(content, voiceCode, listener)
                    }

                })


            } else {
                listener.OnError("speech not Supported")
            }
        } else {
            listener.OnError("No Internet Connection")
        }


    }

    fun getTextfromImage(path: Uri?, listener: OnCameraTextListener) {
        var englishResult: String = ""
        var chineseResult: String = ""
        var devanagariResult: String = ""
        var japaneseResult: String = ""
        var koreanResult: String = ""
        val stringBuilder = StringBuilder()
        if (isInternetConnected(context)) {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val chineseRecognier =
                TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            val devanagariRecognier =
                TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
            val japaneseRecognier =
                TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            val koreanRecognier =
                TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            val image = InputImage.fromFilePath(context, Uri.parse(path.toString()))

            //English Recognizer
            recognizer.process(image).addOnCompleteListener {
                listener.onResult(it.getResult())
            }.addOnFailureListener {
                listener.OnFailed()
            }
            //chinese Recognizer
            chineseRecognier.process(image).addOnCompleteListener {
                chineseResult = it.result.text.toString()
                stringBuilder.append(chineseResult + "._.")

                Log.d("recognizer_srtz", chineseResult.toString())


            }.addOnFailureListener {
                listener.OnFailed()
            }
            //devenagari Recognizer
            devanagariRecognier.process(image).addOnCompleteListener {
                devanagariResult = it.result.text.toString()
                stringBuilder.append(devanagariResult + "._.")


            }.addOnFailureListener {
                listener.OnFailed()
            }
            //japanese Recognizer
            japaneseRecognier.process(image).addOnCompleteListener {
                japaneseResult = it.result.text.toString()
                stringBuilder.append(japaneseResult + "._.")

            }.addOnFailureListener {
                listener.OnFailed()
            }
            //korean Recognizer
            koreanRecognier.process(image).addOnCompleteListener {
                koreanResult = it.result.text.toString()

                stringBuilder.append(koreanResult)

            }.addOnFailureListener {
                listener.OnFailed()
            }

            if (recognizer.process(image).isComplete) {
                Toast.makeText(context, "com", Toast.LENGTH_SHORT).show()
            }
            Log.d("recognizer_srtz23", chineseResult.toString())
            arrayOf(englishResult, chineseResult, devanagariResult, japaneseResult, koreanResult)
            val stringArray = stringBuilder.toString().split(",_@1")
//            listener.onResult(checkStringSize(arrayOf(englishResult,chineseResult,devanagariResult,japaneseResult,koreanResult)))
            if (executor.isShutdown) {
            }
        } else {
            listener.OnFailed()
        }
    }

    private fun dividestrings(index: Int, content: String): String {
        if (index >= content.length) {
            return " "
        }
        val halfString = content.substring(index, 154)
        val endIndex = halfString.length
        return dividestrings(endIndex, content)
    }

    private fun initspeech(mediaPlayer: MediaPlayer) {
        mediaPlayer.reset()
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
    }

    fun stopSpeech() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.reset()
        }
    }

    private fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager!!.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!!
            .state == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.state == NetworkInfo.State.CONNECTED
    }

    private fun checkStringSize(array: Array<String>): String {
        var longestStr = ""
        for (item in array) {
            if (item.length > longestStr.length) {
                Log.d("loop_recgo", item.toString())
                longestStr = item.toString()
            }
        }
        return longestStr
    }


}

