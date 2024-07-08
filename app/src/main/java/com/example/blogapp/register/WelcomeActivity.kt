package com.example.blogapp.register

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.blogapp.MainActivity
import com.example.blogapp.R
import com.example.blogapp.databinding.ActivityWelcomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class WelcomeActivity : AppCompatActivity() {
          private val binding:ActivityWelcomeBinding by lazy {
              ActivityWelcomeBinding.inflate(layoutInflater);
          }
    private lateinit var    auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth=FirebaseAuth.getInstance()
        binding.loginButton.setOnClickListener{
            val intent =Intent(this,SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.registerButton.setOnClickListener{
            val intent =Intent(this,SignUpActivity::class.java)
          //  intent.putExtra("action","register")
            startActivity(intent)
            finish()
        }


    }

    override fun onStart() {
        super.onStart()
        val currentUser:FirebaseUser? = auth.currentUser
        if(currentUser!=null)
        {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

    }
}