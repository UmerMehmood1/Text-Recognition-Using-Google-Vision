package com.example.test

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.test.databinding.ActivityCameraScreenBinding

class CameraScreen : AppCompatActivity() {
    lateinit var binding: ActivityCameraScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }
    private fun loadFrag(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.fragment_view, fragment).commit()
    }
    private fun setListeners(){
        binding.backButton.setOnClickListener{finish()}
        binding.selectionLanguageOne.setOnClickListener{}
        binding.selectionLanguageTwo.setOnClickListener{}
    }

    override fun onResume() {
        super.onResume()
        loadFrag(CameraPictureFragment(supportFragmentManager))
    }
}