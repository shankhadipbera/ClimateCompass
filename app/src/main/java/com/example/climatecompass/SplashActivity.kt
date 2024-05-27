package com.example.climatecompass

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.climatecompass.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private val binding: ActivitySplashBinding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
            Handler().postDelayed({
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            },2500)


    }
}