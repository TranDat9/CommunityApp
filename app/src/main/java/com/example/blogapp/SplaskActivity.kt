package com.example.blogapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.blogapp.register.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class SplaskActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splask)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this,WelcomeActivity::class.java))
        },300)

    }


}